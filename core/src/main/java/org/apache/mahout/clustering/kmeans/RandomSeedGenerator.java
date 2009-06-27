package org.apache.mahout.clustering.kmeans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.mahout.matrix.Vector;

import java.io.IOException;
import java.util.Random;


/**
 * Given an Input Path containing a {@link org.apache.hadoop.io.SequenceFile}, randomly select k vectors
 * and write them to the output file as a {@link org.apache.mahout.clustering.kmeans.Cluster} representing
 * the initial centroid to use.
 * <p/>
 *
 */
public class RandomSeedGenerator {
  private transient static Log log = LogFactory.getLog(RandomSeedGenerator.class);
  public static final String K = "k";

  public static Path runJob(String input, String output,
                            int k ) throws IOException, IllegalAccessException, InstantiationException {
    // delete the output directory
    JobConf conf = new JobConf(RandomSeedGenerator.class);
    Path outPath = new Path(output);
    FileSystem fs = FileSystem.get(outPath.toUri(), conf);
    if (fs.exists(outPath)) {
      fs.delete(outPath, true);
    }
    fs.mkdirs(outPath);
    Path outFile = new Path(outPath, "part-randomSeed");
    if (fs.exists(outFile) == true){
      log.warn("Deleting " + outFile);
      fs.delete(outFile, false);
    }
    boolean newFile = fs.createNewFile(outFile);
    if (newFile == true){
      SequenceFile.Reader reader = new SequenceFile.Reader(fs, new Path(input), conf);
      Writable key = (Writable) reader.getKeyClass().newInstance();
      Vector value = (Vector) reader.getValueClass().newInstance();
      SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, outFile, Text.class, Cluster.class);
      Random random = new Random();
    int count = 0;

    while (reader.next(key, value) && count < k){
      if (random.nextBoolean() == true){
        if (log.isInfoEnabled()) {
          log.info("Selected: " + value.asFormatString());
        }
        writer.append(new Text(key.toString()), new Cluster(value));
        count++;
      }
    }
    log.info("Wrote " + count + " vectors to " + outFile);
    reader.close();
    writer.close();
    }

    return outFile;
  }
}
