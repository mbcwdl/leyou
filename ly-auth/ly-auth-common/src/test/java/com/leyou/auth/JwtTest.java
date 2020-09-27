package com.leyou.auth;

import com.leyou.auth.payload.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/26 9:41
 */
public class JwtTest {

    private static final String pubKeyPath = "D:/rsa.pub";

    private static final String priKeyPath = "D:/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTYwMTA4NTA0MH0.V0tH47i8LTl4gns5kcTZHDksHPavBVxPtGhDUMVm-ZmsHQbyYoO0oiM4ZdTFx69Mc7kEBr4-xdpHpoX_jw8fYM9RWL5EEcur_p7KUbpBL6CJpOhGXBO9gMYYiuPjc_YVt6UHTtCQ230bdwI-FGb2n9_DzC8iZS1hZTOcqVh86_Y";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
