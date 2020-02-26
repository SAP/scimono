import com.sap.scimono.callback.users.UsersCallback;
import com.sap.scimono.entity.Email;
import com.sap.scimono.entity.Meta;
import com.sap.scimono.entity.User;
import com.sap.scimono.entity.paging.PageInfo;
import com.sap.scimono.entity.paging.PagedResult;
import com.sap.scimono.entity.patch.PatchBody;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class Users implements UsersCallback {
    @Override
    public User getUserByUsername(String userName) {
        return null;
    }

    @Override
    public User getUser(String userId) {
        return null;
    }

    @Override
    public PagedResult<User> getUsers(PageInfo pageInfo, String filter) {
        User.Builder userBuilder = new User.Builder();
        userBuilder.setId("user1");

        Email.Builder emailBuilder = new Email.Builder();
        emailBuilder.setDisplay("Ivan Petrov <ivan.petrov@examples.com>");
        emailBuilder.setValue("ivan.petrov@examples.com");
        emailBuilder.setPrimary(true);
        userBuilder.addEmail(emailBuilder.build());

        return new PagedResult<>(1, Arrays.asList(new User[]{userBuilder.build()}));
    }

    @Override
    public User createUser(User user) {
        return user;
    }

    @Override
    public User updateUser(User user) {
        return user;
    }

    @Override
    public void patchUser(String userId, PatchBody patchBody, Meta userMeta) {

    }

    @Override
    public void deleteUser(String userId) {

    }

    @Override
    public Optional<String> generateId() {
        return Optional.of(UUID.randomUUID().toString());
    }
}
