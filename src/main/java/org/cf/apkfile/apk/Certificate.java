package org.cf.apkfile.apk;

import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.cf.apkfile.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class Certificate {

    private final Collection<SubjectAndIssuerRdns> allRdns;

    private final Collection<String> publicKeyHashes;
    private final Collection<String> signatureHashes;

    public Certificate(InputStream certStream) throws CMSException, IOException, CertificateException, NoSuchAlgorithmException {
        publicKeyHashes = new LinkedList<>();
        signatureHashes = new LinkedList<>();
        byte[] certBytes = Utils.readFully(certStream);
        allRdns = extractAll(certBytes);
        hashCertificates(certBytes);
    }

    public Collection<SubjectAndIssuerRdns> getAllRdns() {
        return allRdns;
    }

    public Collection<String> getPublicKeyHashes() {
        return publicKeyHashes;
    }

    public Collection<String> getSignatureHashes() {
        return signatureHashes;
    }

    private static Collection<SubjectAndIssuerRdns> extractAll(byte[] certBytes) throws CMSException, IOException {
        List<SubjectAndIssuerRdns> allRdns = new LinkedList<>();
        CMSSignedData data = new CMSSignedData(certBytes);
        Collection<X509CertificateHolder> matches = data.getCertificates().getMatches(null);
        for (X509CertificateHolder holder : matches) {
            allRdns.add(buildRdns(holder.getSubject(), holder.getIssuer()));
        }

        return allRdns;
    }

    private void hashCertificates(byte[] certBytes) throws IOException, NoSuchAlgorithmException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<? extends java.security.cert.Certificate> certs = cf.generateCertificates(new ByteArrayInputStream(certBytes));
        for (java.security.cert.Certificate c : certs) {
            byte[] publicKeyHash = Utils.sha1(new ByteArrayInputStream(c.getPublicKey().getEncoded()));
            publicKeyHashes.add(Utils.bytesToHex(publicKeyHash));
            byte[] signatureHash = Utils.sha1(new ByteArrayInputStream(((X509Certificate) c).getSignature()));
            signatureHashes.add(Utils.bytesToHex(signatureHash));
        }
    }

    private static SubjectAndIssuerRdns buildRdns(X500Name subject, X500Name issuer) {
        Hashtable defaultSymbols = null;
        try {
            Field f = BCStyle.class.getDeclaredField("DefaultSymbols");
            f.setAccessible(true);
            defaultSymbols = (Hashtable) f.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> subjectMap = new HashMap<>();
        for (RDN rdn : subject.getRDNs()) {
            for (AttributeTypeAndValue typeAndValue : rdn.getTypesAndValues()) {
                String type = defaultSymbols == null ? typeAndValue.getType().toString() : (String) defaultSymbols.get(typeAndValue.getType());
                subjectMap.put(type, typeAndValue.getValue().toString());
            }
        }


        Map<String, String> issuerMap = new HashMap<>();
        for (RDN rdn : subject.getRDNs()) {
            for (AttributeTypeAndValue typeAndValue : rdn.getTypesAndValues()) {
                String type = defaultSymbols == null ? typeAndValue.getType().toString() : (String) defaultSymbols.get(typeAndValue.getType());
                issuerMap.put(type, typeAndValue.getValue().toString());
            }
        }

        return new SubjectAndIssuerRdns(subjectMap, issuerMap);
    }

    public static class SubjectAndIssuerRdns {

        private final Map<String, String> issuerRdns;
        private final Map<String, String> subjectRdns;

        SubjectAndIssuerRdns(Map<String, String> subjectRdns, Map<String, String> issuerRdns) {
            this.subjectRdns = subjectRdns;
            this.issuerRdns = issuerRdns;
        }

        public Map<String, String> getIssuerRdns() {
            return issuerRdns;
        }

        public Map<String, String> getSubjectRdns() {
            return subjectRdns;
        }

        public String toString() {
            return "SUBJECT: " + Arrays.toString(subjectRdns.entrySet().toArray()) + "\nISSUER: " + Arrays.toString(issuerRdns.entrySet().toArray());
        }
    }
}
