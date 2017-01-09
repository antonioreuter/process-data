package com.reuter.data.batch.writers;

import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.WriterNotOpenException;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.batch.support.transaction.TransactionAwareBufferedWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Attention: I didn't write this class entirely by myself. I've copied some parts from Internet and did some adjustments.
 * @param <T>
 */

@Slf4j
public class JsonFileItemWriter<T> extends AbstractItemStreamItemWriter<T>
    implements ResourceAwareItemWriterItemStream<T>, InitializingBean {

  private static final String DEFAULT_ITEM_SEPARATOR = System.getProperty("line.separator");
  private static final String COMMA_SEPARATOR = ",";
  private static final String WRITTEN_STATISTICS_NAME = "written";
  private static final String RESTART_DATA_NAME = "current.count";
  private String encoding = OutputState.DEFAULT_CHARSET;

  private OutputState state;
  private Resource resource;
  private boolean appendAllowed = false;
  private boolean forceSync = false;
  private boolean saveState = true;
  private boolean shouldDeleteIfExists = true;
  private boolean shouldDeleteIfEmpty = false;
  private boolean transactional = true;

  private static String defaultRootNodeValue = null;

  private String itemSeparator;
  private LineAggregator<T> lineAggregator;
  private JsonHeaderFooterCallback headerFooterCallback;

  /**
   * Default constructor. When called, the root node value expected is supposed to be null.
   */
  public JsonFileItemWriter(){
    this(defaultRootNodeValue);
  }

  /**
   * Constructor with the root node's value.
   *
   */
  public JsonFileItemWriter(String rootNode) {
    this.setExecutionContextName(ClassUtils
        .getShortName(JsonFileItemWriter.class));

    headerFooterCallback = new JsonHeaderFooterCallback();
    headerFooterCallback.setRootNode(rootNode);
    this.setJsonItemSeparator(COMMA_SEPARATOR);
  }


  @Override
  public void write(List<? extends T> items) throws Exception {
    if (!getOutputState().isInitialized()) {
      throw new WriterNotOpenException(
          "Writer must be open before it can be written to");
    }

    OutputState state = getOutputState();
    StringBuilder jsonOutput = new StringBuilder();
    int jsonItemCount = 0;
    Iterator<? extends T> it = items.iterator();
    while (it.hasNext()) {
      if (it.hasNext() && jsonItemCount == 0 && state.jsonObjectsWritten > 0) {
        jsonOutput.append(itemSeparator);
      }
      T item = it.next();

      jsonOutput.append(lineAggregator.aggregate(item));
      if (it.hasNext()) {
        jsonOutput.append(itemSeparator);
      }
      jsonItemCount++;
    }
    try {
      state.write(jsonOutput.toString());
    } catch (IOException e) {
      throw new WriteFailedException(
          "Could not write data.  The file may be corrupt.", e);
    }
    state.jsonObjectsWritten += jsonItemCount;

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(lineAggregator, "A JsonItemAggregator must be provided.");
    if (appendAllowed) {
      shouldDeleteIfExists = false;
    }
  }

  @Override
  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setJsonItemSeparator(String jsonSeparator) {
    this.itemSeparator = jsonSeparator;
  }

  public Resource getResource() {
    return resource;
  }

  public void setLineAggregator(LineAggregator<T> aggregator) {
    this.lineAggregator = aggregator;
  }

  public void setShouldDeleteIfEmpty(boolean shouldDeleteIfEmpty) {
    this.shouldDeleteIfEmpty = shouldDeleteIfEmpty;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setAppendAllowed(boolean appendAllowed) {
    this.appendAllowed = appendAllowed;
  }

  public void setShouldDeleteIfExists(boolean shouldDeleteIfExists) {
    this.shouldDeleteIfExists = shouldDeleteIfExists;
  }

  /**
   * Initialize the reader. This method may be called multiple times before
   * close is called.
   *
   * @see ItemStream#open(ExecutionContext)
   */
  @Override
  public void open(ExecutionContext executionContext)
      throws ItemStreamException {
    Assert.notNull(resource, "The resource must be set");
    if (!getOutputState().isInitialized()) {
      this.doOpen(executionContext);
    }
  }

  @Override
  public void update(ExecutionContext executionContext) {
    if (state == null) {
      throw new ItemStreamException(
          "ItemStream not open or already closed.");
    }
    Assert.notNull(executionContext, "ExecutionContext must not be null");
    if (saveState) {
      try {
        executionContext.putLong(
            getExecutionContextKey(RESTART_DATA_NAME),
            state.position());
      } catch (IOException e) {
        throw new ItemStreamException(
            "ItemStream does not return current position properly",
            e);
      }
      executionContext.putLong(
          getExecutionContextKey(WRITTEN_STATISTICS_NAME),
          state.jsonObjectsWritten);
    }
  }

  private void doOpen(ExecutionContext executionContext)
      throws ItemStreamException {
    OutputState outputState = getOutputState();
    if (executionContext
        .containsKey(getExecutionContextKey(RESTART_DATA_NAME))) {
      outputState.restoreFrom(executionContext);
    }
    try {
      outputState.initializeBufferedWriter();
    } catch (IOException ioe) {
      throw new ItemStreamException("Failed to initialize writer", ioe);
    }
    if (outputState.lastMarkedByteOffsetPosition == 0
        && !outputState.appending) {
      if (headerFooterCallback != null) {
        try {
          headerFooterCallback
              .writeHeader(outputState.outputBufferedWriter);
          outputState.write(DEFAULT_ITEM_SEPARATOR);
        } catch (IOException e) {
          throw new ItemStreamException(
              "Could not write headers.  The file may be corrupt.",
              e);
        }
      }
    }
  }

  /**
   * @see ItemStream#close()
   */
  @Override
  public void close() {
    if (state != null) {
      try {
        if (headerFooterCallback != null
            && state.outputBufferedWriter != null) {

          headerFooterCallback.writeFooter(state.outputBufferedWriter);
          state.outputBufferedWriter.flush();
        }
      } catch (IOException e) {
        throw new ItemStreamException(
            "Failed to write footer before closing", e);
      } finally {
        state.close();
        if (state.jsonObjectsWritten == 0 && shouldDeleteIfEmpty) {
          try {
            resource.getFile().delete();
          } catch (IOException e) {
            throw new ItemStreamException(
                "Failed to delete empty file on close", e);
          }
        }
        state = null;
      }
    }
  }

  private OutputState getOutputState() {
    if (state == null) {
      File file;
      try {
        file = resource.getFile();
      } catch (IOException e) {
        throw new ItemStreamException(
            "Could not convert resource to file: [" + resource
                + "]", e);
      }
      Assert.state(!file.exists() || file.canWrite(),
          "Resource is not writable: [" + resource + "]");
      state = new OutputState();
      state.setDeleteIfExists(shouldDeleteIfExists);
      state.setAppend(appendAllowed);
      state.setEncoding(encoding);
    }
    return state;
  }

  /**
   * Encapsulates the runtime state of the writer. All state changing
   * operations on the writer go through this class.
   */
  private class OutputState {
    // default encoding for writing to output files - set to UTF-8.
    private static final String DEFAULT_CHARSET = "UTF-8";

    private FileOutputStream os;

    // The bufferedWriter over the file channel that is actually written
    Writer outputBufferedWriter;

    FileChannel fileChannel;

    // this represents the charset encoding (if any is needed) for the
    // output file
    String encoding = DEFAULT_CHARSET;

    boolean restarted = false;

    long lastMarkedByteOffsetPosition = 0;

    long jsonObjectsWritten = 0;

    boolean shouldDeleteIfExists = true;

    boolean initialized = false;

    private boolean append = false;

    private boolean appending = false;

    /**
     * Return the byte offset position of the cursor in the output file as a
     * long integer.
     */
    public long position() throws IOException {
      long pos = 0;

      if (fileChannel == null) {
        return 0;
      }

      outputBufferedWriter.flush();
      pos = fileChannel.position();
      if (transactional) {
        pos += ((TransactionAwareBufferedWriter) outputBufferedWriter)
            .getBufferSize();
      }

      return pos;

    }

    /**
     * @param append
     */
    public void setAppend(boolean append) {
      this.append = append;
    }

    /**
     * @param executionContext
     */
    public void restoreFrom(ExecutionContext executionContext) {
      lastMarkedByteOffsetPosition = executionContext
          .getLong(getExecutionContextKey(RESTART_DATA_NAME));
      jsonObjectsWritten = executionContext
          .getLong(getExecutionContextKey(WRITTEN_STATISTICS_NAME));
      if (shouldDeleteIfEmpty && jsonObjectsWritten == 0) {
        // previous execution deleted the output file because no items
        // were written
        restarted = false;
        lastMarkedByteOffsetPosition = 0;
      } else {
        restarted = true;
      }
    }

    /**
     * @param shouldDeleteIfExists
     */
    public void setDeleteIfExists(boolean shouldDeleteIfExists) {
      this.shouldDeleteIfExists = shouldDeleteIfExists;
    }

    /**
     * @param encoding
     */
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    /**
     * Close the open resource and reset counters.
     */
    public void close() {

      initialized = false;
      restarted = false;
      try {
        if (outputBufferedWriter != null) {
          outputBufferedWriter.close();
        }
      } catch (IOException ioe) {
        throw new ItemStreamException(
            "Unable to close the the ItemWriter", ioe);
      } finally {
        if (!transactional) {
          closeStream();
        }
      }
    }

    private void closeStream() {
      try {
        if (fileChannel != null) {
          fileChannel.close();
        }
      } catch (IOException ioe) {
        throw new ItemStreamException(
            "Unable to close the the ItemWriter", ioe);
      } finally {
        try {
          if (os != null) {
            os.close();
          }
        } catch (IOException ioe) {
          throw new ItemStreamException(
              "Unable to close the the ItemWriter", ioe);
        }
      }
    }

    /**
     * @param jsonObject
     * @throws IOException
     */
    public void write(String jsonObject) throws IOException {
      if (!initialized) {
        initializeBufferedWriter();
      }

      outputBufferedWriter.write(jsonObject);
      outputBufferedWriter.flush();
    }

    /**
     * Truncate the output at the last known good point.
     *
     * @throws IOException
     */
    public void truncate() throws IOException {
      fileChannel.truncate(lastMarkedByteOffsetPosition);
      fileChannel.position(lastMarkedByteOffsetPosition);
    }

    /**
     * Creates the buffered writer for the output file channel based on
     * configuration information.
     *
     * @throws IOException
     */
    private void initializeBufferedWriter() throws IOException {

      File file = resource.getFile();
      FileUtils.setUpOutputFile(file, restarted, append,
          shouldDeleteIfExists);

      os = new FileOutputStream(file.getAbsolutePath(), true);
      fileChannel = os.getChannel();

      outputBufferedWriter = getBufferedWriter(fileChannel, encoding);
      outputBufferedWriter.flush();

      if (append) {
        // Bug in IO library? This doesn't work...
        // lastMarkedByteOffsetPosition = fileChannel.position();
        if (file.length() > 0) {
          appending = true;
          // Don't write the headers again
        }
      }

      Assert.state(outputBufferedWriter != null);
      // in case of restarting reset position to last committed point
      if (restarted) {
        checkFileSize();
        truncate();
      }

      initialized = true;
    }

    public boolean isInitialized() {
      return initialized;
    }

    /**
     * Returns the buffered writer opened to the beginning of the file
     * specified by the absolute path name contained in absoluteFileName.
     */
    private Writer getBufferedWriter(FileChannel fileChannel,
                                     String encoding) {
      try {
        final FileChannel channel = fileChannel;
        if (transactional) {
          TransactionAwareBufferedWriter writer = new TransactionAwareBufferedWriter(
              channel, new Runnable() {
            @Override
            public void run() {
              closeStream();
            }
          });

          writer.setEncoding(encoding);
          writer.setForceSync(forceSync);
          return writer;
        } else {
          Writer writer = new BufferedWriter(Channels.newWriter(
              fileChannel, encoding)) {
            @Override
            public void flush() throws IOException {
              super.flush();
              if (forceSync) {
                channel.force(false);
              }
            }
          };

          return writer;
        }
      } catch (UnsupportedCharsetException ucse) {
        throw new ItemStreamException(
            "Bad encoding configuration for output file "
                + fileChannel, ucse);
      }
    }

    /**
     * Checks (on setState) to make sure that the current output file's size
     * is not smaller than the last saved commit point. If it is, then the
     * file has been damaged in some way and whole task must be started over
     * again from the beginning.
     *
     * @throws IOException
     *             if there is an IO problem
     */
    private void checkFileSize() throws IOException {
      long size = -1;

      outputBufferedWriter.flush();
      size = fileChannel.size();

      if (size < lastMarkedByteOffsetPosition) {
        throw new ItemStreamException(
            "Current file size is smaller than size at last commit");
      }
    }

  }

  private class JsonHeaderFooterCallback {
    private JsonWriter jsonWriter;
    private String rootNode;

    public void writeHeader(Writer writer) throws IOException {
      this.jsonWriter = new JsonWriter(writer);
      if (rootNode != null && !rootNode.isEmpty()){
        jsonWriter.beginObject().name(rootNode).beginArray();
      }else {
        jsonWriter.beginArray();
      }
    }

    public void writeFooter(Writer writer) throws IOException {
      if (rootNode != null && !rootNode.isEmpty()){
        jsonWriter.endArray().endObject();
      }else {
        jsonWriter.endArray();
      }
      jsonWriter.close();
    }

    public void setRootNode(String rootNode) {
      this.rootNode = rootNode;
    }
  }

}