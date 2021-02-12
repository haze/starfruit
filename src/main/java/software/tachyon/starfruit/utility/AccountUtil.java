package software.tachyon.starfruit.utility;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.util.Session;
import org.apache.commons.lang3.StringUtils;

import java.net.Proxy;

// Thanks to Jordin / Energetic
public class AccountUtil {

    private static final YggdrasilAuthenticationService ygg = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");

    public static Session createSession(String username, String password) throws AuthenticationException {
        UserAuthentication yggdrasilUserAuthentication = constructUserAuthentication(username, password);

        yggdrasilUserAuthentication.logIn();

        GameProfile selectedProfile = yggdrasilUserAuthentication.getSelectedProfile();

        return new Session(selectedProfile.getName(), UUIDTypeAdapter.fromUUID(selectedProfile.getId()),
                yggdrasilUserAuthentication.getAuthenticatedToken(), "legacy" // Why "legacy"?
        );
    }

    private static UserAuthentication constructUserAuthentication(String username, String password) {
//        YggdrasilUserAuthentication yggdrasilUserAuthentication = new YggdrasilUserAuthentication(AccountUtil.ygg,
//                Agent.MINECRAFT);
        final YggdrasilAuthenticationService yggdrasilUserAuthentication = new YggdrasilAuthenticationService(Proxy.NO_PROXY, StringUtils.EMPTY);
        final UserAuthentication userAuthentication = yggdrasilUserAuthentication.createUserAuthentication(Agent.MINECRAFT);

        userAuthentication.setUsername(username);
        userAuthentication.setPassword(password);

        return userAuthentication;
    }

}
