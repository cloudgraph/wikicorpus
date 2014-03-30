package org.cloudgraph.examples.wikicorpus.web.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.cloudgraph.examples.wikicorpus.web.util.BeanFinder;

  
public class ThemeSwitcherBean {  
          
    private Map<String, String> allThemes;  
    private Map<String, String> availableThemes;  
      
    private List<Theme> advancedThemes;  
      
    private String highConstrastTheme = "casablanca";  
    private String normalConstrastTheme = "humanity";  
    private String theme = normalConstrastTheme;  
    
      
    private GuestPreferences gp;  
    
    public ThemeSwitcherBean() {
    	this.gp = new GuestPreferences();
    }
  
    public void setGp(GuestPreferences gp) {  
        this.gp = gp;  
    }  
      
    public Map<String, String> getAllThemes() {  
        return allThemes;  
    }  
    
    public Map<String, String> getAvailableThemes() {  
        return availableThemes;  
    }  
  
    public String getTheme() {  
        return theme;  
    }  
  
    public void setTheme(String theme) {  
        this.theme = theme;  
    }  
            
    public void saveTheme() {  
    	BeanFinder finder = new BeanFinder();
        gp.setTheme(this.theme);  
    }  
  
  
    @PostConstruct  
    public void init() {  
        theme = gp.getTheme();  
          
        advancedThemes = new ArrayList<Theme>();  
        advancedThemes.add(new Theme("aristo", "aristo.png"));  
        advancedThemes.add(new Theme("cupertino", "cupertino.png"));  
        advancedThemes.add(new Theme("trontastic", "trontastic.png"));  
 
        availableThemes = new TreeMap<String, String>();  
        availableThemes.put("Normal Contrast", this.normalConstrastTheme);  
        availableThemes.put("High Contrast ", this.highConstrastTheme);  
        
        allThemes = new TreeMap<String, String>();  
        allThemes.put("Aristo", "aristo");  
        allThemes.put("Afterdark ", "afterdark ");  
        allThemes.put("Afternoon", "afternoon");  
        allThemes.put("Afterwork", "afterwork");  
        allThemes.put("Black-Tie", "black-tie");  
        allThemes.put("Blitzer", "blitzer");  
        allThemes.put("Bluesky", "bluesky");  
        allThemes.put("Casablanca", "casablanca");  
        allThemes.put("Cruze", "cruze");  
        allThemes.put("Cupertino", "cupertino");  
        allThemes.put("Dark-Hive", "dark-hive");  
        allThemes.put("Delta", "delta");  
        allThemes.put("Dot-Luv", "dot-luv");  
        allThemes.put("Eggplant", "eggplant");  
        allThemes.put("Excite-Bike", "excite-bike");  
        allThemes.put("Flick", "flick");  
        allThemes.put("Glass-X", "glass-x");  
        allThemes.put("Home", "home");  
        allThemes.put("Hot-Sneaks", "hot-sneaks");  
        allThemes.put("Humanity", "humanity");  
        allThemes.put("Le-Frog", "le-frog");  
        allThemes.put("Midnight", "midnight");  
        allThemes.put("Mint-Choc", "mint-choc");  
        allThemes.put("Overcast", "overcast");  
        allThemes.put("Pepper-Grinder", "pepper-grinder");  
        allThemes.put("Redmond", "redmond");  
        allThemes.put("Rocket", "rocket");  
        allThemes.put("Sam", "sam");  
        allThemes.put("Smoothness", "smoothness");  
        allThemes.put("South-Street", "south-street");  
        allThemes.put("Start", "start");  
        allThemes.put("Sunny", "sunny");  
        allThemes.put("Swanky-Purse", "swanky-purse");  
        allThemes.put("Trontastic", "trontastic");  
        allThemes.put("Twitter bootstrap", "twitter bootstrap");  
        allThemes.put("UI-Darkness", "ui-darkness");  
        allThemes.put("UI-Lightness", "ui-lightness");  
        allThemes.put("Vader", "vader");  
    }  
      
    public List<Theme> getAdvancedThemes() {  
        return advancedThemes;  
    }  
} 