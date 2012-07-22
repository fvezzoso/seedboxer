package com.superdownloader.proeasy.mule.processors;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.superdownloader.proeasy.core.logic.UsersController;
import com.superdownloader.proeasy.mule.logic.DownloadsQueueManager;


/**
 * @author jdavison
 *
 */
@Component
public class FileReceiver implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileReceiver.class);

	@Autowired
	private DownloadsQueueManager queueManager;

	@Autowired
	private UsersController usersController;

	private Pattern pattern = null;

	@Value("${proeasy.includePattern}")
	public void setPattern(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Message msg = exchange.getIn();

		Matcher m = pattern.matcher((String) msg.getHeader(Exchange.FILE_NAME));
		if (m.matches()) {
			String username = m.group(1);
			String filepath = (String) msg.getHeader(Exchange.FILE_PATH);
			int userId = usersController.getUserId(username);

			for (String path : getLines(filepath)) {
				String realPath = path.replaceFirst("file://", ""); // Removes prefix of Flexget
				queueManager.push(userId, realPath);
			}
		} else {
			throw new Exception("The file doesn't compile with the pattern.");
		}
	}

	private List<String> getLines(String filePath) {
		List<String> lines = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String str;
			while ((str = in.readLine()) != null) {
				lines.add(str);
			}
			in.close();
		} catch (IOException e) {
			LOGGER.error("Cannot open file", e);
		}
		return lines;
	}

}
