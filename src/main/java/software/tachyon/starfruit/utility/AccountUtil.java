package software.tachyon.starfruit.utility;

import net.minecraft.client.util.Session;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.Agent;
import java.net.Proxy;

// Thanks to Jordin / Energetic
public class AccountUtil {

    private static final YggdrasilAuthenticationService ygg = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");

    public static Session createSession(String username, String password) throws AuthenticationException {
        YggdrasilUserAuthentication yggdrasilUserAuthentication = constructUserAuthentication(username, password);

        yggdrasilUserAuthentication.logIn();

        GameProfile selectedProfile = yggdrasilUserAuthentication.getSelectedProfile();

        return new Session(selectedProfile.getName(), UUIDTypeAdapter.fromUUID(selectedProfile.getId()),
                yggdrasilUserAuthentication.getAuthenticatedToken(), "legacy" // Why "legacy"?
        );
    }

    private static YggdrasilUserAuthentication constructUserAuthentication(String username, String password) {
        YggdrasilUserAuthentication yggdrasilUserAuthentication = new YggdrasilUserAuthentication(AccountUtil.ygg,
                Agent.MINECRAFT);

        yggdrasilUserAuthentication.setUsername(username);
        yggdrasilUserAuthentication.setPassword(password);

        return yggdrasilUserAuthentication;
    }

}
