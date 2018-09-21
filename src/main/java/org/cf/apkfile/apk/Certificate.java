package org.cf.apkfile.apk;

import org.cf.apkfile.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedList;

public class Certificate {

    private final Collection<ApkCertificate> apkCertificates;

    public Certificate(InputStream certStream) throws IOException, CertificateException, NoSuchAlgorithmException {
        apkCertificates = new LinkedList<>();
        byte[] certificateBytes = Utils.readFully(certStream);
        loadCertificates(certificateBytes);
    }

    public Collection<ApkCertificate> getCertificates() {
        return apkCertificates;
    }

    private void loadCertificates(byte[] certificateBytes) throws IOException, NoSuchAlgorithmException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<X509Certificate> certificates = (Collection<X509Certificate>) cf.generateCertificates(new ByteArrayInputStream(certificateBytes));
        for (X509Certificate certificate : certificates) {
            byte[] publicKeyHash = Utils.sha1(new ByteArrayInputStream(certificate.getPublicKey().getEncoded()));
            byte[] signatureHash = Utils.sha1(new ByteArrayInputStream(certificate.getSignature()));
            String subjectPrincipal = certificate.getSubjectX500Principal().getName();
            String issuerPrincipal = certificate.getIssuerX500Principal().getName();
            String publicKeyHashString = Utils.bytesToHex(publicKeyHash);
            String signatureHashString = Utils.bytesToHex(signatureHash);
            ApkCertificate apkCertificate = new ApkCertificate(subjectPrincipal, issuerPrincipal, publicKeyHashString, signatureHashString);
            apkCertificates.add(apkCertificate);
        }
    }

    public class ApkCertificate {
        private final String subjectPrincipal;
        private final String issuerPrincipal;
        private final String publicKeyHash;
        private final String signatureHash;

        ApkCertificate(String subjectPrincipal, String issuerPrincipal, String publicKeyHash, String signatureHash) {
            this.subjectPrincipal = subjectPrincipal;
            this.issuerPrincipal = issuerPrincipal;
            this.publicKeyHash = publicKeyHash;
            this.signatureHash = signatureHash;
        }

        public String getSubjectPrincipal() {
            return subjectPrincipal;
        }

        public String getIssuerPrincipal() {
            return issuerPrincipal;
        }

        public String getPublicKeyHash() {
            return publicKeyHash;
        }

        public String getSignatureHash() {
            return signatureHash;
        }
    }
}
