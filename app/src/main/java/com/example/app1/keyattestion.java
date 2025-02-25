package com.example.app1;

import static com.google.common.base.Functions.forMap;
import static com.google.common.collect.Collections2.transform;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.math.BigInteger;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;


public class keyattestion {
    static final String ASN1_OID = "1.3.6.1.4.1.11129.2.1.17";
    static final int ATTESTATION_VERSION_INDEX = 0;
    static final int ATTESTATION_SECURITY_LEVEL_INDEX = 1;
    static final int KEYMASTER_VERSION_INDEX = 2;
    static final int KEYMASTER_SECURITY_LEVEL_INDEX = 3;
    static final int ATTESTATION_CHALLENGE_INDEX = 4;
    static final int UNIQUE_ID_INDEX = 5;
    static final int SW_ENFORCED_INDEX = 6;
    static final int TEE_ENFORCED_INDEX = 7;



    public String checkcertchain() {
        StringBuilder KeyDescription = new StringBuilder("KeyDescription");
        StringBuilder AuthorizationList = new StringBuilder("AuthorizationList");
        try {
            // 从 Android Keystore 中获取密钥
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            // 生成一个非对称密钥对（RSA）如果还没有的话
            if (!keyStore.containsAlias("ec_test_key")) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
                keyPairGenerator.initialize(
                        new KeyGenParameterSpec.Builder(
                                "ec_test_key",
                                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                                .setDigests(KeyProperties.DIGEST_SHA256,
                                        KeyProperties.DIGEST_SHA384,
                                        KeyProperties.DIGEST_SHA512)
                                .setAttestationChallenge("hello world".getBytes(StandardCharsets.UTF_8))
                                .build());
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
            }

            Certificate[] certs = keyStore.getCertificateChain("ec_test_key");
            X509Certificate x509Cert = (X509Certificate) certs[0];
            byte[] attestationExtensionBytes = x509Cert.getExtensionValue(ASN1_OID);
            ASN1Sequence seq = getAsn1SequenceFromBytes(attestationExtensionBytes);

            String attestationVersion = attestationVersionToString(getIntegerFromAsn1(seq.getObjectAt(ATTESTATION_VERSION_INDEX)));
            String attestationSecurityLevel = securityLevelToString(getIntegerFromAsn1(seq.getObjectAt(ATTESTATION_SECURITY_LEVEL_INDEX)));
            String keymasterVersion = keymasterVersionToString(getIntegerFromAsn1(seq.getObjectAt(KEYMASTER_VERSION_INDEX)));
            String keymasterSecurityLevel = securityLevelToString(getIntegerFromAsn1(seq.getObjectAt(KEYMASTER_SECURITY_LEVEL_INDEX)));

            KeyDescription.append("\nattestationVersion:").append(attestationVersion);
            KeyDescription.append("\nattestationSecurityLevel:").append(attestationSecurityLevel);
            KeyDescription.append("\nkeymasterVersion:").append(keymasterVersion);
            KeyDescription.append("\nkeymasterSecurityLevel:").append(keymasterSecurityLevel);

            byte[] attestationChallenge = getByteArrayFromAsn1(seq.getObjectAt(ATTESTATION_CHALLENGE_INDEX));
            byte[] uniqueId = getByteArrayFromAsn1(seq.getObjectAt(UNIQUE_ID_INDEX));

            String stringChallenge =
                    attestationChallenge != null ? new String(attestationChallenge) : "null";
            if (CharMatcher.ascii().matchesAllOf(stringChallenge)) {
                KeyDescription.append("\nattestationChallenge:").append(stringChallenge);
            } else {
                KeyDescription.append("\nattestationChallenge:").append(BaseEncoding.base64().encode(attestationChallenge));
            }
            if (uniqueId != null) {
                KeyDescription.append("\nUnique ID:").append(BaseEncoding.base64().encode(uniqueId));
            }

            System.out.println(x509Cert);

            //AuthorizationList(seq.getObjectAt(SW_ENFORCED_INDEX));
            AuthorizationList.append(AuthorizationList(seq.getObjectAt(TEE_ENFORCED_INDEX)));

            StringBuilder result = new StringBuilder();
            result.append(KeyDescription).append("\n\n").append(AuthorizationList);
            return result.toString();

        } catch (NoSuchAlgorithmException | CertificateException | IOException |
                 NoSuchProviderException |
                 InvalidAlgorithmParameterException |
                 java.security.KeyStoreException e) {
            Log.w("checkcertchainException", e.getMessage(),e);
        }
        return "解析失败";
    }

    public static byte[] getByteArrayFromAsn1(ASN1Encodable asn1Encodable)
            throws CertificateParsingException {
        if (!(asn1Encodable instanceof DEROctetString)) {
            throw new CertificateParsingException("Expected DEROctetString");
        }
        ASN1OctetString derOctectString = (ASN1OctetString) asn1Encodable;
        return derOctectString.getOctets();
    }

    public static int getIntegerFromAsn1(ASN1Encodable asn1Value)
            throws CertificateParsingException {
        if (asn1Value instanceof ASN1Integer) {
            return bigIntegerToInt(((ASN1Integer) asn1Value).getValue());
        } else if (asn1Value instanceof ASN1Enumerated) {
            return bigIntegerToInt(((ASN1Enumerated) asn1Value).getValue());
        } else {
            throw new CertificateParsingException(
                    "Integer value expected, " + asn1Value.getClass().getName() + " found.");
        }
    }
    private static int bigIntegerToInt(BigInteger bigInt) throws CertificateParsingException {
        if (bigInt.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
                || bigInt.compareTo(BigInteger.ZERO) < 0) {
            throw new CertificateParsingException("INTEGER out of bounds");
        }
        return bigInt.intValue();
    }

    public static ASN1Sequence getAsn1SequenceFromBytes(byte[] bytes)
            throws CertificateParsingException {
        try (ASN1InputStream asn1InputStream = new ASN1InputStream(bytes)) {
            return getAsn1SequenceFromStream(asn1InputStream);
        } catch (IOException e) {
            throw new CertificateParsingException("Failed to parse SEQUENCE", e);
        }
    }

    public static ASN1Sequence getAsn1SequenceFromStream(final ASN1InputStream asn1InputStream)
            throws IOException, CertificateParsingException {
        ASN1Primitive asn1Primitive = asn1InputStream.readObject();
        if (!(asn1Primitive instanceof ASN1OctetString)) {
            throw new CertificateParsingException(
                    "Expected octet stream, found " + asn1Primitive.getClass().getName());
        }
        try (ASN1InputStream seqInputStream = new ASN1InputStream(
                ((ASN1OctetString) asn1Primitive).getOctets())) {
            asn1Primitive = seqInputStream.readObject();
            if (!(asn1Primitive instanceof ASN1Sequence)) {
                throw new CertificateParsingException(
                        "Expected sequence, found " + asn1Primitive.getClass().getName());
            }
            return (ASN1Sequence) asn1Primitive;
        }
    }

    public static Set<Integer> getIntegersFromAsn1Set(ASN1Encodable set)
            throws CertificateParsingException {
        if (!(set instanceof ASN1Set)) {
            throw new CertificateParsingException(
                    "Expected set, found " + set.getClass().getName());
        }

        ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
        for (Enumeration<?> e = ((ASN1Set) set).getObjects(); e.hasMoreElements();) {
            builder.add(getIntegerFromAsn1((ASN1Integer) e.nextElement()));
        }
        return builder.build();
    }


//--------------------------------------------------KeyDescription---------------------------------------------------------
    public static String attestationVersionToString(int version) {
        return switch (version) {
            case 1 -> "Keymaster 2.0";
            case 2 -> "Keymaster 3.0";
            case 3 -> "Keymaster 4.0";
            case 4 -> "Keymaster 4.1";
            case 100 -> "KeyMint 1.0";
            case 200 -> "KeyMint 2.0";
            case 300 -> "KeyMint 3.0";
            default -> "Unknown (" + version + ")";
        };
    }
    public static String keymasterVersionToString(int version) {
        return switch (version) {
            case 0 -> "Keymaster 0.2 or 0.3";
            case 1 -> "Keymaster 1.0";
            case 2 -> "Keymaster 2.0";
            case 3 -> "Keymaster 3.0";
            case 4 -> "Keymaster 4.0";
            case 41 -> "Keymaster 4.1";
            case 100 -> "KeyMint 1.0";
            case 200 -> "KeyMint 2.0";
            case 300 -> "KeyMint 3.0";
            default -> "Unknown (" + version + ")";
        };
    }
    public static String securityLevelToString(int attestationSecurityLevel) {
        return switch (attestationSecurityLevel) {
            case 0 -> "Software";
            case 1 -> "TEE";
            case 2 -> "StrongBox";
            default -> "Unknown (" + attestationSecurityLevel + ")";
        };
    }
//--------------------------------------------------AuthorizationList---------------------------------------------------------
// Keymaster tag classes
    public static final int KM_ENUM = 1 << 28;
    public static final int KM_ENUM_REP = 2 << 28;
    public static final int KM_UINT = 3 << 28;
    public static final int KM_UINT_REP = 4 << 28;
    public static final int KM_ULONG = 5 << 28;
    public static final int KM_DATE = 6 << 28;
    public static final int KM_BOOL = 7 << 28;
    public static final int KM_BYTES = 9 << 28;
    public static final int KM_ULONG_REP = 10 << 28;
    // Tag class removal mask
    public static final int KEYMASTER_TAG_TYPE_MASK = 0x0FFFFFFF;
    // Keymaster tags
    public static final int KM_TAG_PURPOSE = KM_ENUM_REP | 1;
    public static final int KM_TAG_ALGORITHM = KM_ENUM | 2;
    public static final int KM_TAG_KEY_SIZE = KM_UINT | 3;
    public static final int KM_TAG_BLOCK_MODE = KM_ENUM_REP | 4;
    public static final int KM_TAG_DIGEST = KM_ENUM_REP | 5;
    public static final int KM_TAG_PADDING = KM_ENUM_REP | 6;
    public static final int KM_TAG_CALLER_NONCE = KM_BOOL | 7;
    public static final int KM_TAG_MIN_MAC_LENGTH = KM_UINT | 8;
    public static final int KM_TAG_KDF = KM_ENUM_REP | 9;
    public static final int KM_TAG_EC_CURVE = KM_ENUM | 10;
    public static final int KM_TAG_RSA_PUBLIC_EXPONENT = KM_ULONG | 200;
    public static final int KM_TAG_RSA_OAEP_MGF_DIGEST = KM_ENUM_REP | 203;
    public static final int KM_TAG_ROLLBACK_RESISTANCE = KM_BOOL | 303;
    public static final int KM_TAG_EARLY_BOOT_ONLY = KM_BOOL | 305;
    public static final int KM_TAG_ACTIVE_DATETIME = KM_DATE | 400;
    public static final int KM_TAG_ORIGINATION_EXPIRE_DATETIME = KM_DATE | 401;
    public static final int KM_TAG_USAGE_EXPIRE_DATETIME = KM_DATE | 402;
    public static final int KM_TAG_USAGE_COUNT_LIMIT = KM_UINT | 405;
    public static final int KM_TAG_NO_AUTH_REQUIRED = KM_BOOL | 503;
    public static final int KM_TAG_USER_AUTH_TYPE = KM_ENUM | 504;
    public static final int KM_TAG_AUTH_TIMEOUT = KM_UINT | 505;
    public static final int KM_TAG_ALLOW_WHILE_ON_BODY = KM_BOOL | 506;
    public static final int KM_TAG_TRUSTED_USER_PRESENCE_REQUIRED = KM_BOOL | 507;
    public static final int KM_TAG_TRUSTED_CONFIRMATION_REQUIRED = KM_BOOL | 508;
    public static final int KM_TAG_UNLOCKED_DEVICE_REQUIRED = KM_BOOL | 509;
    public static final int KM_TAG_ALL_APPLICATIONS = KM_BOOL | 600;
    public static final int KM_TAG_APPLICATION_ID = KM_BYTES | 601;
    public static final int KM_TAG_CREATION_DATETIME = KM_DATE | 701;
    public static final int KM_TAG_ORIGIN = KM_ENUM | 702;
    public static final int KM_TAG_ROLLBACK_RESISTANT = KM_BOOL | 703;
    public static final int KM_TAG_ROOT_OF_TRUST = KM_BYTES | 704;
    public static final int KM_TAG_OS_VERSION = KM_UINT | 705;
    public static final int KM_TAG_OS_PATCHLEVEL = KM_UINT | 706;
    public static final int KM_TAG_ATTESTATION_APPLICATION_ID = KM_BYTES | 709;
    public static final int KM_TAG_ATTESTATION_ID_BRAND = KM_BYTES | 710;
    public static final int KM_TAG_ATTESTATION_ID_DEVICE = KM_BYTES | 711;
    public static final int KM_TAG_ATTESTATION_ID_PRODUCT = KM_BYTES | 712;
    public static final int KM_TAG_ATTESTATION_ID_SERIAL = KM_BYTES | 713;
    public static final int KM_TAG_ATTESTATION_ID_IMEI = KM_BYTES | 714;
    public static final int KM_TAG_ATTESTATION_ID_MEID = KM_BYTES | 715;
    public static final int KM_TAG_ATTESTATION_ID_MANUFACTURER = KM_BYTES | 716;
    public static final int KM_TAG_ATTESTATION_ID_MODEL = KM_BYTES | 717;
    public static final int KM_TAG_VENDOR_PATCHLEVEL = KM_UINT | 718;
    public static final int KM_TAG_BOOT_PATCHLEVEL = KM_UINT | 719;
    public static final int KM_TAG_DEVICE_UNIQUE_ATTESTATION = KM_BOOL | 720;
    public static final int KM_TAG_IDENTITY_CREDENTIAL_KEY = KM_BOOL | 721;
    public static final int KM_TAG_ATTESTATION_ID_SECOND_IMEI = KM_BYTES | 723;

    private Integer securityLevel;
    private Set<Integer> purposes;
    private Integer algorithm;
    private Integer keySize;
    private Set<Integer> digests;
    private Set<Integer> paddingModes;
    private Integer ecCurve;
    private Long rsaPublicExponent;
    private Set<Integer> mgfDigests;
    private Boolean rollbackResistance;
    private Boolean earlyBootOnly;
    private Date activeDateTime;
    private Date originationExpireDateTime;
    private Date usageExpireDateTime;
    private Integer usageCountLimit;
    private Boolean noAuthRequired;
    private Integer userAuthType;
    private Integer authTimeout;
    private Boolean allowWhileOnBody;
    private Boolean trustedUserPresenceReq;
    private Boolean trustedConfirmationReq;
    private Boolean unlockedDeviceReq;
    private Boolean allApplications;
    private String applicationId;
    private Date creationDateTime;
    private Integer origin;
    private Boolean rollbackResistant;
    //private RootOfTrust rootOfTrust;
    private Integer osVersion;
    private Integer osPatchLevel;
    //private AttestationApplicationId attestationApplicationId;
    private String brand;
    private String device;
    private String product;
    private String serialNumber;
    private String imei;
    private String meid;
    private String manufacturer;
    private String model;
    private Integer vendorPatchLevel;
    private Integer bootPatchLevel;
    private Boolean deviceUniqueAttestation;
    private Boolean identityCredentialKey;
    private String secondImei;


    public static final int KM_PURPOSE_ENCRYPT = 0;
    public static final int KM_PURPOSE_DECRYPT = 1;
    public static final int KM_PURPOSE_SIGN = 2;
    public static final int KM_PURPOSE_VERIFY = 3;
    public static final int KM_PURPOSE_WRAP = 5;
    public static final int KM_PURPOSE_AGREE_KEY = 6;
    public static final int KM_PURPOSE_ATTEST_KEY = 7;
    private static final ImmutableMap<Integer, String> purposeMap = ImmutableMap
            .<Integer, String>builder()
            .put(KM_PURPOSE_DECRYPT, "DECRYPT")
            .put(KM_PURPOSE_ENCRYPT, "ENCRYPT")
            .put(KM_PURPOSE_SIGN, "SIGN")
            .put(KM_PURPOSE_VERIFY, "VERIFY")
            .put(KM_PURPOSE_WRAP, "WRAP")
            .put(KM_PURPOSE_AGREE_KEY, "AGREE KEY")
            .put(KM_PURPOSE_ATTEST_KEY, "ATTEST KEY")
            .build();


    public String AuthorizationList(ASN1Encodable asn1Encodable) throws CertificateParsingException {
        StringBuilder s = new StringBuilder();
        if (!(asn1Encodable instanceof ASN1Sequence sequence)) {
            throw new CertificateParsingException("Expected sequence for authorization list, found "
                    + asn1Encodable.getClass().getName());
        }
        for (var entry : sequence) {
            if (!(entry instanceof ASN1TaggedObject taggedObject)) {
                throw new CertificateParsingException(
                        "Expected tagged object, found " + entry.getClass().getName());
            }
            int tag = taggedObject.getTagNo();
            var value = taggedObject.getBaseObject().toASN1Primitive();
            Log.d("AuthorizationList", "Parsing tag: [" + tag + "], value: [" + value + "]");
            switch (tag) {
                default:
                    //throw new CertificateParsingException("Unknown tag " + tag + " found");
                    System.out.println(tag);
                    break;

                case KM_TAG_PURPOSE & KEYMASTER_TAG_TYPE_MASK:
                    purposes = getIntegersFromAsn1Set(value);
                    break;
            }
        }
        if (purposes != null && !purposes.isEmpty()) {
            s.append("\nPurposes: ").append(transform(purposes, forMap(purposeMap, "Unknown")));
        }
        return s.toString();
    }
    //--------------------------------tag---------------------------------------




}
