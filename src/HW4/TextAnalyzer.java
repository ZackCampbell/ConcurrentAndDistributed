import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {

    // Replace "?" with your own output key / value types
    // The four template data types are:
    //     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
    public static class TextMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
        {
            String line = value.toString();
	    line = line.toLowerCase();
            line = line.replaceAll("[^a-zA-Z0-9]", " ");
            line = line.trim().replaceAll(" +", " ");
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            ArrayList<Text> words = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                Text word = new Text(tokenizer.nextToken());
                if (!words.contains(word)) {
                    words.add(word);
                }
            }
            for (int i = 0; i < words.size(); i++) {
                for (int j = 0; j < words.size(); j++) {
                    if (i != j) {
                        context.write(new Text(words.get(i) + " " + words.get(j)), one);
                    }
                }
            }
            // Implementation of you mapper function
        }
    }

    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper
    public static class TextCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
        public void reduce(Text key, Iterable<IntWritable> tuples, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you combiner function
        }
    }

    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function
    public static class TextReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private final static Text emptyText = new Text("");

        public void reduce(Text key, Iterable<IntWritable> queryInts, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you reducer function
            int sum = 0;
            for (IntWritable value : queryInts) {
                sum += value.get();
            }
            IntWritable result = new IntWritable(sum);
            context.write(key, result);


            // Write out the results; you may change the following example
            // code to fit with your reducer function.
            //   Write out each edge and its weight
//	    Text value = new Text();
//            for(String neighbor: map.keySet()){
//                String weight = map.get(neighbor).toString();
//                value.set(" " + neighbor + " " + weight);
//                context.write(key, value);
//            }
//            //   Empty line for ending the current context key
//            context.write(emptyText, emptyText);
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();

        // Create job
        Job job = new Job(conf, "zcc254_ka25635"); // Replace with your EIDs
        job.setJarByClass(TextAnalyzer.class);

        // Setup MapReduce job
        job.setMapperClass(TextMapper.class);

	// set local combiner class
        job.setCombinerClass(TextReducer.class);
	// set reducer class
	job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        //job.setMapOutputKeyClass(?.class);
        //job.setMapOutputValueClass(?.class);

        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputFormatClass(TextOutputFormat.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    // Do not modify the main method
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
        System.exit(res);
    }

    // You may define sub-classes here. Example:
    // public static class MyClass {
    //
    // }
}



