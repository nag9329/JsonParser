/**
 * 
 */
package parser;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author nagarjunaramagiri
 *
 */
public class MyMain {
	static JsonNode rootObject;
	static String commaSeparator = "\\,";
	static String dotSeparator = "\\.";
	/**
	 * 
	 */
	public MyMain() {
		// TODO Auto-generated constructor stub
	}

	private static void getJSONAsString() {
		ObjectMapper mapper = new ObjectMapper();
		File inputFile = new File("/Users/nagarjunaramagiri/Documents/Java/JsonParserJava/src/parser/expected_response.json");
		try {
			rootObject = mapper.readTree(inputFile);
			String str = mapper.writeValueAsString(rootObject);
			JsonStringEncoder enc = JsonStringEncoder.getInstance();
			System.out.println(new String(enc.quoteAsString(str)));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getJSONAsString();
		//executeParsing();
	}

	private static void executeParsing() {
		ObjectMapper mapper = new ObjectMapper();
		File inputFile = new File("/Users/nagarjunaramagiri/Documents/Java/JsonParserJava/src/parser/expected_response.json");
		try {
			rootObject = mapper.readTree(inputFile);
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		String featureFileInput = "[buildOutputResponse.rulesFired.ruleId,buildOutputResponse.rulesFired.description],buildOutputResponse.rulesFired.code";
		String featureFileOutput = "[Rule Set: RS_AssignGlobalVariables  - RuleName: InitializationOfGlobalVariables,efgh],null";


		//		String[] inputKeys = featureFileInput.split(commaSeparator);
		String[] inputKeys = featureFileInput.split(commaSeparator+"+(?![^\\[]*\\])");
		String[] expectedValues = featureFileOutput.split(commaSeparator+"+(?![^\\[]*\\])");

		for (int i = 0; i < inputKeys.length; i++) {
			String inputKey = inputKeys[i];
			String expectedValue = expectedValues[i];
			if (inputKey.contains("[")) {
				Matcher keyMatcher = Pattern.compile("\\[(.*?)\\]").matcher(inputKey);
				Matcher valueMatcher = Pattern.compile("\\[(.*?)\\]").matcher(expectedValue);
				String arrayInputKey = "";
				String arrayInputValue = "";
				while(keyMatcher.find()) {
					arrayInputKey = keyMatcher.group(1);
				}
				while(valueMatcher.find()) {
					arrayInputValue = valueMatcher.group(1);
				}
				String[] arrayInputKeys = arrayInputKey.split(commaSeparator);
				String[] arrayInputValues = arrayInputValue.split(commaSeparator);

				System.out.println(hasCombinationMatch(arrayInputKeys, arrayInputValues));
			} else {
				System.out.println(hasMatch(inputKey, expectedValue));
			}
		}
	}

	private static boolean hasCombinationMatch(String[] inputKeys, String[] expectedValues) {
		String[] splitKeys = inputKeys[0].split(dotSeparator);
		return Boolean.parseBoolean(getRecursively(0, splitKeys, expectedValues[0], rootObject, inputKeys, expectedValues));	
	}

	private static boolean hasMatch(String inputKey, String expectedValue) {
		String actualValue = findValueOfKey(inputKey, expectedValue);
		return actualValue.equals(expectedValue);
	}

	private static String findValueOfKey(String inputKey, String expectedValue) {
		String[] splitKeys = inputKey.split(dotSeparator);
		return getRecursively(0, splitKeys, expectedValue, rootObject, null, null);	
	}

	private static String getRecursively(int level, String[] splitKeys, String expectedValue, JsonNode obj, String[] inputKeys, String[] expectedValues) {
		String key = splitKeys[level];
		JsonNode node = obj.get(key);
		if(node.isArray()) {
			level = level + 1;
			for (int i = 0; i < node.size(); i++) {
				JsonNode subNode = node.get(i);
				String res = getRecursively(level, splitKeys, expectedValue, subNode, inputKeys, expectedValues);
				if(inputKeys == null) {
					if(res.equals(expectedValue) || i == node.size()-1) {
						return res;
					}
				} else {
					if(res.equals(expectedValue)) {
						for(int j = 0; j<inputKeys.length; j++) {
							String[] _splitKeys = inputKeys[j].split(dotSeparator);
							String _res = subNode.get(_splitKeys[_splitKeys.length-1]).asText();
							if(!_res.equals(expectedValues[j])) {
								return "false";
							}
						}
						return "true";
					} else if (i == node.size()-1) {
						return res.equals("") ? "false" : res;
					}
				}
			}
		} else if(node.isObject()) {
			level = level + 1;
			return getRecursively(level, splitKeys, expectedValue, node, inputKeys, expectedValues);
		} else {
			return node.asText();
		}
		return "";
	}
}
