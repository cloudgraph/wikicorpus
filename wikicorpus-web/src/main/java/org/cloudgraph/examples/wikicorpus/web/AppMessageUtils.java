package org.cloudgraph.examples.wikicorpus.web;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The AppMessageUtils is an adapter to the implementation from myfaces
 * MessageUtils. It is not a part of JSF spec.
 * 
 * org.apache.myfaces.util.MessageUtils
 * 
 * Add any more methods that are needed here
 * 
 */
public class AppMessageUtils {

	private static final Log log = LogFactory.getLog(AppMessageUtils.class);

	private static final String DETAIL_SUFFIX = "_detail";
	private static final String BUNDLE_NAME_KEY = "_bundleNameKey";
	private static Map<String, ResourceBundle> bundleMap = new HashMap<String, ResourceBundle>();;

	public static FacesMessage getMessage(FacesMessage.Severity severity,
			String messageId, Object arg1) {
		return getMessage(severity, messageId, new Object[] { arg1 },
				FacesContext.getCurrentInstance());
	}

	public static FacesMessage getMessage(FacesMessage.Severity severity,
			String messageId, Object[] args) {
		return getMessage(severity, messageId, args, FacesContext
				.getCurrentInstance());
	}

	public static FacesMessage getMessage(FacesMessage.Severity severity,
			String messageId, Object[] args, FacesContext facesContext) {
		FacesMessage message = getMessage(facesContext, messageId, args);
		message.setSeverity(severity);

		return message;
	}

	public static void addMessage(FacesMessage.Severity severity,
			String messageId, Object[] args) {
		addMessage(severity, messageId, args, null, FacesContext
				.getCurrentInstance());
	}

	public static void addMessage(FacesMessage.Severity severity,
			String messageId, Object[] args, FacesContext facesContext) {
		addMessage(severity, messageId, args, null, facesContext);
	}

	public static void addMessage(FacesMessage.Severity severity,
			String messageId, Object[] args, String forClientId) {
		addMessage(severity, messageId, args, forClientId, FacesContext
				.getCurrentInstance());
	}

	public static void addMessage(FacesMessage.Severity severity,
			String messageId, Object[] args, String forClientId,
			FacesContext facesContext) {
		if (log.isTraceEnabled()) {
			log.trace("adding message " + messageId + " for clientId "
					+ forClientId);
		}
		facesContext.addMessage(forClientId, getMessage(severity, messageId,
				args, facesContext));
	}

	public static void setMessageBundle(String bundleName) {
		// store the name of the resource bundle for this user in the session.
		try {
		    HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
		    session.setAttribute(getBundleNameKey(), bundleName);
		}
		catch (Throwable t) {
			log.error("could not set bundle into session", t);
		}
		
		// store the resource bundle in the map, if it's not already there.
		ResourceBundle bundle = (ResourceBundle) bundleMap.get(bundleName);
		if (bundle == null) { // if null then load it and cache it.
			bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault(),
					getCurrentLoader(bundleName));
			bundleMap.put(bundleName, bundle);
		}
	}

	public static ResourceBundle getMessageBundle() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
		String bundleName = (String) session.getAttribute(getBundleNameKey());
		if (bundleName == null) {
			throw new RuntimeException(
					"could not find resource bundle in session from key '"
					    + getBundleNameKey() + "'");
		}
		return (ResourceBundle) bundleMap.get(bundleName);
	}

	public static String getBundleNameKey() {
		String context = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestContextPath();
		return context + BUNDLE_NAME_KEY;
	}

	/**
	 * Uses <code>MessageFormat</code> and the supplied parameters to fill in
	 * the param placeholders in the String.
	 * 
	 * @param locale
	 *            The <code>Locale</code> to use when performing the
	 *            substitution.
	 * @param msgtext
	 *            The original parameterized String.
	 * @param params
	 *            The params to fill in the String with.
	 * @return The updated String.
	 */
	public static String substituteParams(Locale locale, String msgtext,
			Object params[]) {
		String localizedStr = null;
		if (params == null || msgtext == null)
			return msgtext;
		StringBuffer b = new StringBuffer(100);
		MessageFormat mf = new MessageFormat(msgtext);
		if (locale != null) {
			mf.setLocale(locale);
			b.append(mf.format(((Object) (params))));
			localizedStr = b.toString();
		}
		return localizedStr;
	}

	public static FacesMessage getMessage(String messageId, Object params[]) {
		Locale locale = null;
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null && context.getViewRoot() != null) {
			locale = context.getViewRoot().getLocale();
			if (locale == null)
				locale = Locale.getDefault();
		} else {
			locale = Locale.getDefault();
		}
		return getMessage(locale, messageId, params);
	}

	public static FacesMessage getMessage(Locale locale, String messageId,
			Object params[]) {
		String summary = null;
		String detail = null;
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);

		String bundleName = (String) session.getAttribute(getBundleNameKey());
		if (bundleName == null) {
			throw new RuntimeException(
					"Could not look up message from resource bundle since the bundle name is missing from the session.");
		}

		ResourceBundle bundle = (ResourceBundle) bundleMap.get(bundleName);
		if (bundle == null) { // this should of been loaded in
								// setMessageBundle().
			throw new RuntimeException("ResourceBundle '" + bundleName
					+ "' was not found.");
		}

		try {
			summary = bundle.getString(messageId);
		} catch (MissingResourceException e) {
			// NoOp
		}

		if (summary == null) {
			summary = messageId;
		}

		summary = substituteParams(locale, summary, params);

		try {
			detail = substituteParams(locale, bundle.getString(messageId
					+ DETAIL_SUFFIX), params);
		} catch (MissingResourceException e) {
			// NoOp
		}

		return new FacesMessage(summary, detail);
	}

	public static FacesMessage getMessage(FacesContext context, String messageId) {
		return getMessage(context, messageId, ((Object[]) (null)));
	}

	public static FacesMessage getMessage(FacesContext context,
			String messageId, Object params[]) {
		if (context == null || messageId == null)
			throw new NullPointerException(" context " + context
					+ " messageId " + messageId);
		Locale locale = null;
		if (context != null && context.getViewRoot() != null)
			locale = context.getViewRoot().getLocale();
		else
			locale = Locale.getDefault();
		if (null == locale)
			throw new NullPointerException(" locale " + locale);
		FacesMessage message = getMessage(locale, messageId, params);
		if (message != null) {
			return message;
		} else {
			locale = Locale.getDefault();
			return getMessage(locale, messageId, params);
		}
	}

	private static Application getApplication() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			return FacesContext.getCurrentInstance().getApplication();
		} else {
			ApplicationFactory afactory = (ApplicationFactory) FactoryFinder
					.getFactory("javax.faces.application.ApplicationFactory");
			return afactory.getApplication();
		}
	}

	/**
	 * Gets the ClassLoader associated with the current thread. Returns the
	 * class loader associated with the specified default object if no context
	 * loader is associated with the current thread.
	 * 
	 * @param defaultObject
	 *            The default object to use to determine the class loader (if
	 *            none associated with current thread.)
	 * @return ClassLoader
	 */
	protected static ClassLoader getCurrentLoader(Object defaultObject) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = defaultObject.getClass().getClassLoader();
		}
		return loader;
	}
}