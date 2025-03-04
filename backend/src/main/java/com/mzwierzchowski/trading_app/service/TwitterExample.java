package com.mzwierzchowski.trading_app.service;

import twitter4j.*;


public class TwitterExample {
  public static void main(String[] args) throws TwitterException {

    String consumerKey = "consumerKey";
    String consumerSecret = "consumerSecret";
    String accessToken = "accessToken-j53E5lGxR1SNDNt774fG19CcgCW1w3";
    String accessTokenSecret = "accessTokenSecret";

      var twitter = Twitter.newBuilder()
              .oAuthConsumer(consumerKey, consumerSecret)
              .oAuthAccessToken(accessToken, accessTokenSecret)
              .build();
      twitter.v1().tweets().updateStatus("Hello Twitter API!");
  }
  //        oauth.consumerSecret=[consumer secret]
  //        oauth.accessToken=[access token]
  //        oauth.accessTokenSecret=[access token secret]
  //        ConfigurationBuilder cb = new ConfigurationBuilder();
  //        cb.setDebugEnabled(true)
  //                .setOAuthConsumerKey("7p2gFBpt8ivPCKvBO2FGq74Yl")
  //                .setOAuthConsumerSecret("XOKlRWXK0fNZv25qe4e2S9MQf4H14OJxk0SawgKNhb2r2DU3pr")
  //                .setOAuthAccessToken("1897011657899188224-j53E5lGxR1SNDNt774fG19CcgCW1w3")
  //                .setOAuthAccessTokenSecret("CpvEhYPmcwFNiNDQlO3BxqNudRAv2TitiufS0zODppJmI");
  //
  ////        TwitterFactory tf = new TwitterFactory(cb.build());
  ////        Twitter twitter = tf.getInstance();
  //
  //        Twitter twitter = Twitter.getInstance();
  //        twitter.v1().tweets().updateStatus("Hello Twitter API!");
  //
  //        try {
  //            Query query = new Query("Tesla");
  //            QueryResult result;
  //            do {
  //                result = twitter.search(query);
  //                for (Status status : result.getTweets()) {
  //                    System.out.println("@" + status.getUser().getScreenName() + ": " +
  // status.getText());
  //                }
  //            } while ((query = result.nextQuery()) != null);
  //        } catch (TwitterException te) {
  //            te.printStackTrace();
  //        }
  //    }
}
