package edu.nyu.jet.models;

import edu.nyu.jet.aceJet.AnchoredPath;
import edu.nyu.jet.lex.Stemmer;

import java.util.ArrayList;
import java.util.List;

/**
 * MatcherPath is a dependency path to be matched by the PathMatcher. It is an ordered list of MatcherNodes.
 *
 * @author yhe
 * @version 1.0
 */
public class MatcherPath {
	List<MatcherNode> nodes = new ArrayList<MatcherNode>();
	String arg1Type = "UNK";
	String arg2Type = "UNK";
	String arg1Subtype = "UNK";
	String arg2Subtype = "UNK";
	String relationType = "NONE";
	Stemmer stemmer = Stemmer.getDefaultStemmer();

	public MatcherPath(String pathString) {
		// System.out.println(pathString);
		nodes.clear();
		String[] parts = pathString.split("--");
		if (parts.length == 3) {
			String arg1 = parts[0].trim();
			String arg2 = parts[2].trim();

			// if pattern contains subtypes
			if (arg1.contains(":") && arg2.contains(":")) {
				arg1Type = arg1.split(":")[0].trim();
				arg1Subtype = arg1.split(":")[1].trim();
				arg2Type = arg2.split(":")[0].trim();
				arg2Subtype = arg2.split(":")[1].trim();
			} else if (arg1.contains(":") && !arg2.contains(":")) {
				arg1Type = arg1.split(":")[0].trim();
				arg1Subtype = arg1.split(":")[1].trim();
				arg2Type = arg2.split(":")[0].trim();
			} else if (!arg1.contains(":") && arg2.contains(":")) {
				arg1Type = arg1.split(":")[0].trim();
				arg2Type = arg2.split(":")[0].trim();
				arg2Subtype = arg2.split(":")[1].trim();
			} else {
				arg1Type = arg1;
				arg2Type = arg2;
			}

			parts = parts[1].split(":");

			for (int i = 0; i < (parts.length - 1) / 2; i++) {
				MatcherNode node = new MatcherNode(parts[2 * i], stemmer.getStem(parts[2 * i + 1], "UNK"));
				nodes.add(node);
				// System.out.println(node.label + node.token);
			}
			MatcherNode node = new MatcherNode(parts[parts.length - 1], "END_OF_PATH");
			nodes.add(node);
			// System.out.println(node.label + node.token);
		}
	}

	public MatcherPath(AnchoredPath path) {
		nodes.clear();
		String pathString = path.toString();
		String[] parts = pathString.split("--");
		if (parts.length == 3) {
			arg1Type = parts[0].trim();
			arg2Type = parts[2].trim();
			parts = parts[1].split(":");
			for (int i = 0; i < (parts.length - 1) / 2; i++) {
				MatcherNode node = new MatcherNode(parts[2 * i], stemmer.getStem(parts[2 * i + 1], "UNK"));
				nodes.add(node);
			}
			MatcherNode node = new MatcherNode(parts[parts.length - 1], "END_OF_PATH");
			nodes.add(node);
		}
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public String getRelationType() {
		return relationType;
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public int length() {
		return nodes.size();
	}

	/**
	 * get the path of the pattern without the arguments
	 */
	public String getPath() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nodes.size() - 1; i++) {
			sb.append(nodes.get(i).label).append(":");
			sb.append(nodes.get(i).token).append(":");
		}
		sb.append(nodes.get(nodes.size() - 1).label);

		return sb.toString();
	}

	@Override
	public String toString() {
		if (nodes.size() == 0) {
			return arg1Type + "-- --" + arg2Type;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(arg1Type).append("--");
		for (int i = 0; i < nodes.size() - 1; i++) {
			sb.append(nodes.get(i).label).append(":");
			sb.append(nodes.get(i).token).append(":");
		}
		sb.append(nodes.get(nodes.size() - 1).label);
		sb.append("--").append(arg2Type);
		return sb.toString();
	}

	/**
	 * returns the pattern including both types and subtypes
	 */
	public String toStringSubtypes() {
		if (nodes.size() == 0) {
			return arg1Type + ":" + arg1Subtype + "-- --" + arg2Type + ":" + arg2Subtype;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(arg1Type + ":" + arg1Subtype).append("--");
		for (int i = 0; i < nodes.size() - 1; i++) {
			sb.append(nodes.get(i).label).append(":");
			sb.append(nodes.get(i).token).append(":");
		}
		sb.append(nodes.get(nodes.size() - 1).label);
		sb.append("--").append(arg2Type + ":" + arg2Subtype);
		return sb.toString();
	}
}
