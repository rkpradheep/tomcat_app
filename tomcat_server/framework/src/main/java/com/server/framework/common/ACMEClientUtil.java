package com.server.framework.common;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.*;
import org.shredzone.acme4j.exception.AcmeException;

import com.server.framework.job.JobUtil;

public class ACMEClientUtil
{
	private static final URI ACME_SERVER_URI = URI.create("https://acme-v02.api.letsencrypt.org/directory");
	private static Account account;
	private static final Map<String, Triple<Order, Challenge, byte[]>> DOMAIN_IN_MEMORY_META = new HashMap<>();
	private static final Map<String, Integer> DOMAIN_FAILURE_COUNT = new HashMap<>();

	private static final Logger LOGGER = Logger.getLogger(ACMEClientUtil.class.getName());

	static
	{
		try
		{
			KeyPair acmeClientKeyPair = getACMEClientKeyPair("acme_client");
			Session session = new Session(ACME_SERVER_URI);
			account = findOrCreateAccount(session, acmeClientKeyPair);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred");
		}
	}

	static Map<String, Object> initiate(String domain, byte[] csrFileBytes, boolean isHTTPChallenge) throws Exception
	{
		if(DOMAIN_IN_MEMORY_META.containsKey(domain))
		{
			throw new AppException("Challenge already initiated. Please complete the verification");
		}

		if(DOMAIN_FAILURE_COUNT.getOrDefault(domain, 0) >= 3)
		{
			JobUtil.scheduleJob(() -> DOMAIN_FAILURE_COUNT.remove(domain), DateUtil.ONE_HOUR_IN_MILLISECOND);
			throw new AppException("You have reached the maximum failure retry threshold. Try again later.");
		}

		byte[] parsedCSRBytes = parseCSR(csrFileBytes);

		Order order = account.newOrder()
			.domains(domain)
			.create();

		for(Authorization auth : order.getAuthorizations())
		{
			return isHTTPChallenge ? createHTTPChallenge(auth, domain, parsedCSRBytes, order) : createDNSChallenge(auth, domain, parsedCSRBytes, order);
		}

		throw new AppException("Authorization not available");
	}

	static Map<String, Object> createDNSChallenge(Authorization auth, String domain, byte[] csr, Order order) throws Exception
	{
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("message", "Add the dns TXT record with name and value provided in challenge node for your domain in the DNS setting of your DNS provider");

		Dns01Challenge challenge = (Dns01Challenge) getChallenge(auth, Dns01Challenge.TYPE);

		if(Objects.isNull(challenge))
		{
			throw new AppException("DNS01 Challenge is not available for your domain");
		}
		String dnsRecord = "_acme-challenge." + domain;
		String dnsValue = challenge.getDigest();

		DOMAIN_IN_MEMORY_META.put(domain, new ImmutableTriple<>(order, challenge, csr));
		Map<String, String> challengeTokens = new HashMap<>();
		challengeTokens.put("name", dnsRecord);
		challengeTokens.put("value", dnsValue);

		responseMap.put("challenge", challengeTokens);

		return responseMap;
	}

	static Map<String, Object> createHTTPChallenge(Authorization auth, String domain, byte[] csr, Order order) throws Exception
	{
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("message", "Make your server return the data present in file_content node for HTTP request from the endpoint present in path node");

		Http01Challenge challenge = (Http01Challenge) getChallenge(auth, Http01Challenge.TYPE);

		if(Objects.isNull(challenge))
		{
			throw new AppException("HTTP01 Challenge is not available for your domain");
		}

		DOMAIN_IN_MEMORY_META.put(domain, new ImmutableTriple<>(order, challenge, csr));

		String token = challenge.getToken();

		Map<String, String> challengeTokens = new HashMap<>();
		challengeTokens.put("file_content", challenge.getAuthorization());
		challengeTokens.put("path", "/.well-known/acme-challenge/" + token);

		responseMap.put("challenge", challengeTokens);

		return responseMap;
	}

	static Challenge getChallenge(Authorization auth, String type)
	{
		for(Challenge challenge : auth.getChallenges())
		{
			if(StringUtils.equals(challenge.getType(), type))
			{
				return challenge;
			}
		}
		return null;
	}

	static String verify(String domainName) throws Exception
	{
		try
		{
			if(!DOMAIN_IN_MEMORY_META.containsKey(domainName))
			{
				throw new AppException("No challenge initiated for the given domain yet. Please initiate the challenge first.");
			}

			DOMAIN_FAILURE_COUNT.put(domainName, DOMAIN_FAILURE_COUNT.getOrDefault(domainName, 0) + 1);
			if(DOMAIN_FAILURE_COUNT.getOrDefault(domainName, 0) >= 3)
			{
				final String domain = domainName;
				JobUtil.scheduleJob(() -> DOMAIN_FAILURE_COUNT.remove(domain), DateUtil.ONE_HOUR_IN_MILLISECOND);
			}

			Order order = DOMAIN_IN_MEMORY_META.get(domainName).getLeft();
			Challenge challenge = DOMAIN_IN_MEMORY_META.get(domainName).getMiddle();

			challenge.trigger();

			challenge.update();

			if(challenge.getStatus() != Status.VALID)
			{
				throw new AppException("Challenge failed with error message " + challenge.getError().get().getDetail().get());
			}

			byte[] csr = DOMAIN_IN_MEMORY_META.get(domainName).getRight();
			order.execute(csr);

			if(order.getStatus() != Status.VALID)
			{
				throw new AppException("Challenge failed with error message " + order.getError().get().getDetail().get());
			}
			order.update();

			Certificate certificate = order.getCertificate();
			String plainDomainName = domainName.replaceFirst("\\*\\.", "") + ".pem";
			String path = Util.HOME_PATH + "/tomcat_build/webapps/ROOT/uploads/" + plainDomainName;
			try(FileWriter writer = new FileWriter(path))
			{
				certificate.writeCertificate(writer);
			}

			IOUtils.copy(new FileReader(path), new FileWriter(Util.HOME_PATH + "/uploads/" + plainDomainName));
			return plainDomainName;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			throw e;
		}
		finally
		{
			DOMAIN_IN_MEMORY_META.remove(domainName);
		}
	}

	private static KeyPair getACMEClientKeyPair(String filename) throws Exception
	{
		String rootPath = Util.HOME_PATH + "/tomcat_build/webapps/ROOT/WEB-INF/";
		String privateKeyFile = rootPath + filename + "_private_key.pem";
		String publicKeyFile = rootPath + filename + "_public_key.pem";

		byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
		keyBytes = Base64.getDecoder().decode(keyBytes);
		byte[] publicKeyBytes = Files.readAllBytes(Paths.get(publicKeyFile));
		publicKeyBytes = Base64.getDecoder().decode(publicKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		return new KeyPair(keyFactory.generatePublic(x509EncodedKeySpec), keyFactory.generatePrivate(keySpec));

	}

	private static byte[] parseCSR(byte[] csr) throws Exception
	{
		try
		{
			String csrString = new String(csr);
			csrString = csrString.replaceAll("-----BEGIN CERTIFICATE REQUEST-----", "");
			csrString = csrString.replaceAll("-----END CERTIFICATE REQUEST-----", "");
			csrString = csrString.replaceAll("\n", "");

			return Base64.getDecoder().decode(csrString.getBytes());
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			throw new AppException("Not able to pare the given CSR file. Please ensure the csr file starts with -----BEGIN CERTIFICATE REQUEST---- and ends wih -----END CERTIFICATE REQUEST-----");
		}
	}

	//	private static PKCS10CertificationRequest parseCSR(Reader reader) throws Exception
	//	{
	//		try(PemReader pemReader = new PemReader(reader))
	//		{
	//			PemObject pemObject = pemReader.readPemObject();
	//			byte[] csrBytes = pemObject.getContent();
	//			return new PKCS10CertificationRequest(csrBytes);
	//		}
	//		catch(Exception e)
	//		{
	//			LOGGER.log(Level.SEVERE, "Exception occurred",e);
	//			throw new AppException("Not able to pare the given CSR file. Please ensure the file is in correct format");
	//		}
	//	}

	private static Account findOrCreateAccount(Session session, KeyPair userKeyPair) throws AcmeException
	{
		try
		{
			return new AccountBuilder()
				.onlyExisting()
				.useKeyPair(userKeyPair)
				.create(session);
		}
		catch(AcmeException ex)
		{
			return new AccountBuilder()
				.agreeToTermsOfService()
				.useKeyPair(userKeyPair)
				.create(session);
		}
	}

	//	private static KeyPair getDomainKeyPair(String publicKeyPath, String privateKeyPath) throws Exception
	//	{
	//		return new KeyPair(getPublicKey(publicKeyPath), getPrivateKey(privateKeyPath));
	//	}
	//
	private static KeyPair generateACMEClientKeyPair(String filename) throws Exception
	{
		String privateKeyFile = Util.HOME_PATH + "/tomcat_server/tomcat/webapps/ROOT/WEB-INF/" + filename + "_private_key.pem";
		String publicKeyFile = Util.HOME_PATH + "/tomcat_server/tomcat/webapps/ROOT/WEB-INF/" + filename + "_public_key.pem";

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair keyPair = keyGen.generateKeyPair();
		try(FileOutputStream fos = new FileOutputStream(privateKeyFile))
		{
			fos.write(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()));
		}
		try(FileOutputStream fos = new FileOutputStream(publicKeyFile))
		{
			fos.write(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
		}
		return keyPair;
	}
	//
	//	static PublicKey getPublicKey(String publicKeyPath) throws Exception
	//	{
	//		byte[] keyBytes = Files.readAllBytes(Paths.get(publicKeyPath));
	//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	//		String publicKeyContent = new String(keyBytes);
	//		publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
	//
	//		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
	//		return keyFactory.generatePublic(keySpec);
	//	}
	//
	//	static PrivateKey getPrivateKey(String privateKeyPath) throws Exception
	//	{
	//		byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyPath));
	//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	//		String privateKeyContent = new String(keyBytes);
	//		privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
	//
	//		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
	//		return keyFactory.generatePrivate(keySpec);
	//	}
	//
	//	private static PKCS10CertificationRequest generateCSR(KeyPair keyPair, String domain) throws Exception
	//	{
	//		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	//		X500Name subject = new X500Name("CN=" + "*." + domain);
	//		JcaPKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
	//		JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
	//		ContentSigner signer = csBuilder.build(keyPair.getPrivate());
	//		return p10Builder.build(signer);
	//	}
	//
	//	private static PKCS10CertificationRequest loadCSR(String csrFile) throws Exception
	//	{
	//		return parseCSR(new FileReader(new File(csrFile)));
	//	}
}