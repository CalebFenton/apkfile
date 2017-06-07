package org.cf.apkfile.apk;

import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

public class Certificate {

    private final Collection<SubjectAndIssuerRdns> allRdns;

    public Certificate(InputStream certStream) throws CMSException {
        allRdns = extractAll(certStream);
    }

    public Collection<SubjectAndIssuerRdns> getAllRdns() {
        return allRdns;
    }

    private static Collection<SubjectAndIssuerRdns> extractAll(InputStream certStream) throws CMSException {
        List<SubjectAndIssuerRdns> allRdns = new LinkedList<>();
        CMSSignedData data = new CMSSignedData(certStream);
        Collection<X509CertificateHolder> matches = data.getCertificates().getMatches(null);
        for (X509CertificateHolder holder : matches) {
            allRdns.add(buildRdns(holder.getSubject(), holder.getIssuer()));
        }
        return allRdns;
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
