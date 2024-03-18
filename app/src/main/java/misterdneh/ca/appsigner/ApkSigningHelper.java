package misterdneh.ca.appsigner;

import android.net.Uri;
import android.sun.security.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.TextView;
import com.android.apksig.*;
import com.android.apksig.apk.ApkFormatException;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import java.security.*;
import java.security.cert.*;
import android.sun.security.provider.*;

public class ApkSigningHelper {

    public ApkSigner.Builder builder;
    public KeyStore keys;
    public String alias = "";
    public String password = "";
    public String ksPassword = "";
    public InputStream keypath;
    public X509Certificate cert;
    public String in = "";
    public String signed = "";
    public boolean setV3 = false;
    public boolean setV4 = false;
    public String v4Output = "";
    public Uri inputURI = null;
    public Uri outputURI = null;


    public ApkSigningHelper(InputStream keypath, String alias, String password, String ksPassword, Uri inputURI, Uri outputURI){
        this.keypath = keypath;
        this.alias = alias;
        this.password = password;
        this.ksPassword = ksPassword;
        this.inputURI = inputURI;
        this.outputURI = outputURI;
    }
    private List<ApkSigner.SignerConfig> getConfigs() throws Exception {
        keys = KeyStore.getInstance("JKS",new JavaKeyStoreProvider());
        char[] pass = (password).toCharArray();

        // InputStream is = new FileInputStream(keypath);
        keys.load(keypath, pass);
        cert = (X509Certificate)keys.getCertificate(alias);

        ApkSigner.SignerConfig.Builder builder =
                new ApkSigner.SignerConfig.Builder(alias,
                        (PrivateKey)keys.getKey(alias,ksPassword
                                .toCharArray()),Arrays.asList(cert));

        ArrayList arrayList = new ArrayList();
        arrayList.add(builder.build());
        return arrayList;
    }
    public void sign(){
        try {
            builder = new ApkSigner.Builder(getConfigs());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setProps(builder);
        try {
            builder.build().sign();
        } catch (IOException | ApkFormatException | NoSuchAlgorithmException | InvalidKeyException |
                 SignatureException e) {

            throw new RuntimeException(e);
        }
    }
    public void setProps(ApkSigner.Builder builder){
        builder.setCreatedBy(alias);
        builder.setMinSdkVersion(26);
        builder.setV1SigningEnabled(true);
        builder.setV2SigningEnabled(true);
        builder.setV3SigningEnabled(setV3);
        builder.setV4SigningEnabled(setV4);
        builder.setInputApk(new File(in));
        builder.setOutputApk(new File(signed));
        if(setV4){
            builder.setV4SignatureOutputFile(new File(v4Output));
        }
        builder.setOtherSignersSignaturesPreserved(false);
    }

}




