package com.googlecode.sardine;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;

import com.googlecode.sardine.model.ObjectFactory;
import com.googlecode.sardine.util.SardineException;

/**
 * The factory class is responsible for instantiating the JAXB stuff
 * as well as the instance to SardineImpl.
 *
 * @author jonstevens
 */
public class Factory
{
	/** */
    private final static Factory INSTANCE = new Factory();

	/** */
	protected static Factory instance() { return INSTANCE; }

	/** */
	private final JAXBContext context;

	/** */
	public Factory()
	{
		try
		{
			this.context = JAXBContext.newInstance(ObjectFactory.class);
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the JAXBContext
	 */
	public JAXBContext getContext()
	{
		return this.context;
	}

	/**
	 * Note: the unmarshaller is not thread safe, so it must be
	 * created for every request.
	 *
	 * @return the JAXB Unmarshaller
	 */
	public Unmarshaller getUnmarshaller() throws SardineException
	{
		try
		{
			return this.context.createUnmarshaller();
		}
		catch (JAXBException e)
		{
			throw new SardineException(e);
		}
	}

	/** */
	public Sardine begin() throws SardineException
	{
		return this.begin(null, null, null, null, null);
	}

	/** */
	public Sardine begin(SSLSocketFactory sslSocketFactory) throws SardineException
	{
		return this.begin(null, null, sslSocketFactory);
	}

	/** */
	public Sardine begin(String username, String password) throws SardineException
	{
		return this.begin(username, password, null, null, null);
	}

	/** */
	public Sardine begin(String username, String password, Integer port) throws SardineException
	{
		return this.begin(username, password, null, null, port);
	}

	/** */
	public Sardine begin(String username, String password, HttpRoutePlanner routePlanner) throws SardineException
	{
		return this.begin(username, password, routePlanner);
	}

	/** */
	public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory) throws SardineException
	{
		return this.begin(username, password, sslSocketFactory, null, null);
	}

	/** */
	public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner) throws SardineException
	{
		return this.begin(username, password, sslSocketFactory, routePlanner, null);
	}

	/** */
	public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner, Integer port) throws SardineException
	{
		return new SardineHttpClientImpl(this, username, password, sslSocketFactory, routePlanner, port);
	}
}
