package com.flatironschool.javacs;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Deque;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
    	 * 2. Ignoring external links, links to the current page, or red links
    	 * 3. Stopping when reaching "Philosophy", a page with no links or a page
	 *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
 	
	public static boolean isLinkInParens(String parentString, String link){
		String[] parentSplit = parentString.split("\\(");
		int parensCounter = 0;
		for(int i = 0; i < parentSplit.length; i++){
			if(parentSplit[i].contains(link)) {
				String[] endParensSplit = parentSplit[i].split("\\)");
					for(int j = 0; j < endParensSplit.length; j++){
						if(endParensSplit[j].contains(link)){
							if(parensCounter >= 1){
								return true;
							}
						}
						else{
							parensCounter--; 
						}
					}				
			}
			else {
				parensCounter++;
				if(parentSplit[i].contains(")")){
					parensCounter--;
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) throws IOException {
	
		HashSet<String> visited = new HashSet<String>();
		
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		Elements paragraphs = wf.fetchWikipedia(url);

		int counter = 0;
		boolean foundLink = false;
		boolean linksOnPage = false;
		boolean foundPhilosophy = false;
		String mostRecentLink = "";

		while(!foundPhilosophy){
			paragraphs = wf.fetchWikipedia(url);
			foundLink = false;
			counter = 0;
			while(!foundLink){
				linksOnPage = false;
				for(Element paragraph: paragraphs){
					Element thisPara = paragraphs.get(counter); //what if theres no link in first paragraph
					Elements hrefs  = thisPara.select("a[href]");
					Element e;
					try{	
						e = hrefs.get(0);
					}catch(Exception except){ 
						continue;
					}
					//check if this link is in parens, if it is, then continue to the next
					String link = e.attr("href");
					boolean inParens = isLinkInParens(e.parent().toString(), link);
					int parensCounter = 1;
					while(inParens){
						try{
							e = hrefs.get(parensCounter);
							link = e.attr("href");
							inParens = isLinkInParens(e.parent().toString(), link);
							parensCounter++;
						}
						catch(Exception except1){ 
							continue;
						}
					}
					linksOnPage = true;
					if(!visited.contains(link) && !link.contains("cite_note") && !link.contains("Help:")){
						visited.add(link);
						mostRecentLink = link;
						foundLink = true;
						break;
					}
					else if(visited.contains(link)){
						System.out.println("Error: links have reached a cycle. Philosophy not found.");
						throw new IOException();
					}
					counter++;	
				} //why am i looping through the paragraphs, i could just select hrefs from the doc? 	
				if(!linksOnPage){
					System.out.println("no links on this page!");
					throw new IllegalArgumentException();
				}	
			}			
				
			url = "https://en.wikipedia.org" + mostRecentLink;
			System.out.println(url); 	
	  		if(visited.contains("/wiki/Philosophy")){
				foundPhilosophy = true;
			} 				
		}		
	}
}
