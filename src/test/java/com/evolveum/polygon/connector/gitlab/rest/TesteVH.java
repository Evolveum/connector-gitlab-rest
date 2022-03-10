package com.evolveum.polygon.connector.gitlab.rest;


import org.json.JSONArray;
import org.json.JSONObject;

public class TesteVH {
  public static void main(String[] args) {
  
   // Initialize Variables
   JSONArray groupsWithMPMembers = new JSONArray();
   JSONObject group= new JSONObject();
   

   // Put data
   group.put("id","500");
   group.put("name", "teste");
   
   System.out.println(group);
   
   groupsWithMPMembers.put(group);
    // Print the first item
    System.out.println(groupsWithMPMembers);
  }
}
