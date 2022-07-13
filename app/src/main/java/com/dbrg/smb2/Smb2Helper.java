package com.dbrg.smb2;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.io.InputStreamByteChunkProvider;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.rapid7.client.dcerpc.mssrvs.ServerService;
import com.rapid7.client.dcerpc.mssrvs.dto.NetShareInfo0;
import com.rapid7.client.dcerpc.transport.RPCTransport;
import com.rapid7.client.dcerpc.transport.SMBTransportFactories;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.smb.SmbFile;

/**
 * @author wangying
 * Created on 2019/11/5.
 */
//@Slf4j
@RequiresApi(api = Build.VERSION_CODES.O)
public class Smb2Helper {

//    private final Path TARGET_PATH = Paths.get("D:/SMB2-TEST/", "mySQL.msi");
//    private final String SOURCE_PATH = "C:\\Users\\iceqi\\Desktop\\l.txt";
//    private final String TARGET_DIR = "testDir";
//    private final String SVR_IP = "192.1.1.1";
    public final String DOMAIN = "";
//    public final String USERNAME = "w";
//    public final String PASSWORD = "w";
    public final String USERNAME = "iceqi";
    public final String PASSWORD = "nbc";
    protected CIFSContext context;  // TODO to remove
    protected DiskShare remoteShare;
    protected Session smbSession;

    public static void main(String[] args) throws IOException {

        Smb2Helper s2d = new Smb2Helper();
        s2d.init();
        s2d.testWrite2();

////        read(context);
//        s2d.testWrite(context);

        // access SMB share
//        SMBClient client = new SMBClient();
//
//        try (Connection connection = client.connect("192.1.1.1")) {
//            AuthenticationContext ac = new AuthenticationContext("w", "n".toCharArray(), null);
//            Session session = connection.authenticate(ac);
//
//            session.connectShare()
//            // Connect to Share
//            try (DiskShare share = (DiskShare) session.connectShare("")) {
//                for (FileIdBothDirectoryInformation f : share.list("", "*.TXT")) {
//                    System.out.println("File : " + f.getFileName());
//                }
//            }
//        }

        // list root share folders
//        final SMBClient smbClient = new SMBClient();
//        try (final Connection smbConnection = smbClient.connect(SVR_IP)) {
//            final AuthenticationContext smbAuthenticationContext = new AuthenticationContext("w", "n".toCharArray(), "");
//            final Session session = smbConnection.authenticate(smbAuthenticationContext);
//
////            final RPCTransport transport = SMBTransportFactories.SRVSVC.getTransport(session);
////            final ServerService serverService = new ServerService(transport);
////            // Get1 shares at information level 0
////            final List<NetShareInfo0> shares = serverService.getShares0();
////            for (final NetShareInfo0 share : shares) {
////                System.out.println(share);
////            }
//            List<String> l= checkShares(session);
////            for(String s : l)
////                output(s);
//        }

    }

    public void init() throws IOException {
//         context =  SingletonContext.getInstance().withCredentials(
//                new NtlmPasswordAuthenticator(DOMAIN, USERNAME, PASSWORD));
        SmbConfig c = SmbConfig.builder().withDialects(
                SMB2Dialect.SMB_2_0_2
        ).build();

        SMBClient client = new SMBClient(/*c*/);

//        Connection connection = client.connect("192.1.1.1");
        Connection connection = client.connect("192.1.1.99");
        AuthenticationContext ac = new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), DOMAIN);
        smbSession = connection.authenticate(ac);
        remoteShare = (DiskShare) smbSession.connectShare("test");


            // Connect to Share
//            try (DiskShare share = (DiskShare) session.connectShare("test")) {
//                for (FileIdBothDirectoryInformation f : share.list("", "*.TXT")) {
//                    System.out.println("File : " + f.getFileName());
//                }
//            }
//        }
    }

    public List<String> getRootShares(Session session) throws IOException {
        final RPCTransport transport = SMBTransportFactories.SRVSVC.getTransport(session);
        final ServerService serverService = new ServerService(transport);
        // Get shares at information level 0
        final List<NetShareInfo0> shares = serverService.getShares0();
        List<String> lst = new ArrayList<>();
        for (final NetShareInfo0 share : shares) {
            lst.add(share.getNetName());
        }

        return lst;
    }

    private void testWrite2() throws IOException{
        File smbFile = remoteShare.openFile("text/test.txt",
                EnumSet.of(AccessMask.GENERIC_WRITE, AccessMask.GENERIC_READ),
                null,
                null,
                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                null);
        String localPath = "C:\\Users\\iceqi\\Desktop\\movie\\2.mp4";
        InputStream is = new FileInputStream(localPath);
        smbFile.write(new InputStreamByteChunkProvider(is));
        smbFile.close();
        is.close();
    }

    private void testWrite() throws IOException {
//        String dir = getShareRootURL()/* + TARGET_DIR*/;
//        String targetPath = dir + "/" + "test.txt";
//        SmbFileWriter.createDirectory(dir, context);
//        boolean result = SmbFileWriter.writeSmbFile(SOURCE_PATH, targetPath, context);

        String dir = getShareRootURL();// + "/" + target;
        String t = dir + "/" + "test.txt";
        try (SmbFile file = new SmbFile(t, context)) {
            OutputStream os = null;
            byte[] bytes = new byte[10240];
            for(int i=0; i<bytes.length; i++)
                bytes[i] = 'c';
            try {
                os = file.getOutputStream();
                while(true)
                    os.write(bytes);
//                while (in.read(bytes) != -1) {
//                    os.write(bytes);
//                }
//                return true;
            }finally {
                os.close();
            }
        }
    }

    private void testRead(CIFSContext context) throws IOException {
//        long start = System.currentTimeMillis();
//        SmbFileReader reader = new SmbFileReader();
//        InputStream in = reader.readSmbFile(getShareRootURL() + ShareProperties.FILE_PATH, context);
//        Files.copy(in, TARGET_PATH, StandardCopyOption.REPLACE_EXISTING);
//        long end = System.currentTimeMillis();
    }

    private String getShareRootURL() {
        return "smb://192.1.1.1/test/test";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public InputStream readSmbFile(String path, CIFSContext context) throws IOException {
        try (SmbFile file = new SmbFile(path, context)) {
            return file.getInputStream();
        }
    }

    public SmbFile createDirectory(String targetDir, CIFSContext context) throws MalformedURLException,
            CIFSException {
        try (SmbFile dir = new SmbFile(targetDir, context)) {
            dir.mkdir();
            return dir;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean writeSmbFile(InputStream in, String target, boolean overwrite) throws IOException {
        String t = getShareRootURL() + "/" + target;
        try (SmbFile file = new SmbFile(t, context)) {
            if(!overwrite && file.exists())
                return false;
            OutputStream os = null;
            //TODO SMB2Cre
            try {
                os = file.getOutputStream();
                byte[] bytes = new byte[1024];
                while (in.read(bytes) != -1) {
                    os.write(bytes);
                }
                return true;
            }finally {
                os.close();
            }
        }
    }

    public boolean writeSmbFile2(java.io.File local, String target, boolean overwrite) throws IOException{
//        remoteShare = (DiskShare) smbSession.connectShare("test");
        File smbFile = remoteShare.openFile("/" + target,
                EnumSet.of(AccessMask.GENERIC_WRITE),
                null,
                null,
                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                null);

        int bufSize = remoteShare.getTreeConnect().getSession().getConnection().getNegotiatedProtocol().getMaxWriteSize();
        OutputStream os =  smbFile.getOutputStream();

        InputStream is = new FileInputStream(/*local*/"/storage/emulated/0/1movie/2.mp4");
        smbFile.write(new InputStreamByteChunkProvider(is));
//        byte[] buf = new byte[bufSize];
//        for(int i=0; i<buf.length; i++)
//            buf[i] = 'c';
//        while(true)
//            os.write(buf);
//        int inSize = is.read(buf);
//        while(inSize > 0){
//            os.write(buf, 0, inSize);
//            inSize = is.read(buf);
//        }

        smbFile.close();
        is.close();

        return true;
    }
}
