/**
 * 
 */
package controllers;

import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;

import play.GlobalSettings;

/**
 * @author Steffen
 *
 */
public class Global extends GlobalSettings {


    public void onStart(final Application app) {
		  // OAuth
		  final FacebookClient facebookClient = new FacebookClient("fb_key", "fb_secret");
		  final TwitterClient twitterClient = new TwitterClient("tw_key", "tw_secret");
		  // HTTP
//		  final FormClient formClient = new FormClient("http://localhost:9000/theForm", new SimpleTestUsernamePasswordAuthenticator());
//		  final BasicAuthClient basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());
//		  // CAS
//		  final CasClient casClient = new CasClient();
//		  // casClient.setLogoutHandler(new PlayLogoutHandler());
//		  // casClient.setCasProtocol(CasProtocol.SAML);
//		  // casClient.setGateway(true);
//		  /*final CasProxyReceptor casProxyReceptor = new CasProxyReceptor();
//		  casProxyReceptor.setCallbackUrl("http://localhost:9000/casProxyCallback");
//		  casClient.setCasProxyReceptor(casProxyReceptor);*/
//		  casClient.setCasLoginUrl("http://localhost:8080/cas/login");
//
//		  final Clients clients = new Clients("http://localhost:9000/callback", facebookClient, twitterClient, formClient, basicAuthClient, casClient); // , casProxyReceptor);
//		  Config.setClients(clients);
		}

}
