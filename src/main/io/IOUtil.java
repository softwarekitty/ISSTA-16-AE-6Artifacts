package main.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * so that I don't have to google how to do files in Java again
 * 
 * @author cc
 */
public class IOUtil {

	/**
	 * creates a file with the given content, writing over any existing file.
	 * 
	 * @param file
	 *            The file to write to, created if it does not exist.
	 * @param content
	 *            The content to write into the file.
	 */
	public static void createAndWrite(File file, String content) {
		FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * gets lines from a file into a list of Strings
	 * 
	 * @param filePathString
	 *            Absolute file path, as a String.
	 * @return List of lines in the file.
	 * @throws IOException
	 */
	public static List<String> readLines(String filePathString) throws IOException {
		Path filePath = new File(filePathString).toPath();
		Charset charset = Charset.defaultCharset();
		return Files.readAllLines(filePath, charset);
	}
	
	/**
	 * gets entire file content into one String
	 * 
	 * @param filePathString
	 *            Absolute file path, as a String.
	 * @return content of file as a String.
	 * @throws IOException
	 */
	public static String readFileToString(String filePathString) throws IOException {
		Path filePath = new File(filePathString).toPath();
		return new String(Files.readAllBytes(filePath));
	}
}
