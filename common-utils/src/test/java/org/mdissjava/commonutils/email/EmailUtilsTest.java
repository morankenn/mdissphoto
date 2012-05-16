package org.mdissjava.commonutils.email;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.mail.EmailException;
import org.junit.Test;

public class EmailUtilsTest {

	/*
	 * @Test public void SimpleEmailTest() {
	 * 
	 * try { EmailUtils.sendEmail("jose.maesog@gmail.com", "Sending email Test",
	 * "Hello Jose, Welcome to MDISS Photo", EmailUtils.TEXT); } catch
	 * (EmailException e) { fail("Exception ocurred sending email"); } catch
	 * (IOException e) { fail("Exception ocurred reading the properties"); }
	 * assertTrue("Correct", true); }
	 * 
	 * @Test public void HtmlEmailTest() { try { EmailUtils .sendEmail(
	 * "jose.maesog@gmail.com", "Sending email Test",
	 * "<img src=\"http://4.bp.blogspot.com/_mtyB5sG_n04/S-h6yG_dnLI/AAAAAAAAACc/tAWkdqKRnKo/s1600/gatico.gif\"/><div> Hello Jose use this link <a href=\"www.google.es\">GOOGLE</a></div>"
	 * , EmailUtils.HTML); } catch (EmailException e) {
	 * fail("Exception ocurred sending email"); } catch (IOException e) {
	 * fail("Exception ocurred reading the properties"); } assertTrue("Correct",
	 * true); }
	 */

	@Test
	public void TemplateEmailTest() {
		try {
			EmailUtils.sendValidationEmail("slok69@gmail.com", "Xabier Larrakoetxea", "http://localhost:8080/mdissphoto/p/validate/" + UUID.randomUUID().toString());
			//EmailUtils.sendEmail("jose.maesog@gmail.com", "Prueba Email", "A ver si llega esto", EmailUtils.TEXT);
		} catch (EmailException e) {
			fail("Exception ocurred sending email");
		} catch (IOException e) {
			fail("Exception ocurred reading the properties");
		}
		assertTrue("Correct", true);
	}

}