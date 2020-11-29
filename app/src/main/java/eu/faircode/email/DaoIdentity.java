package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoIdentity {
    @Query(TupleIdentityView.query)
    LiveData<List<TupleIdentityView>> liveIdentityView();

    @Query("SELECT identity.*, account.name AS accountName, folder.id AS drafts" +
            " FROM identity" +
            " JOIN account ON account.id = identity.account" +
            " LEFT JOIN folder ON folder.account = account.id AND folder.type = '" + EntityFolder.DRAFTS + "'")
    LiveData<List<TupleIdentityEx>> liveIdentities();

    @Query("SELECT identity.*, account.name AS accountName, folder.id AS drafts" +
            " FROM identity" +
            " JOIN account ON account.id = identity.account" +
            " JOIN folder ON folder.account = identity.account AND folder.type = '" + EntityFolder.DRAFTS + "'" +
            " AND identity.synchronize" +
            " AND account.synchronize")
    LiveData<List<TupleIdentityEx>> liveComposableIdentities();

    @Query("SELECT identity.*, account.name AS accountName, folder.id AS drafts" +
            " FROM identity" +
            " JOIN account ON account.id = identity.account" +
            " JOIN folder ON folder.account = identity.account AND folder.type = '" + EntityFolder.DRAFTS + "'" +
            " WHERE (:account IS NULL OR account.id = :account)" +
            " AND identity.synchronize" +
            " AND account.synchronize" +
            " ORDER BY account.`order`, account.`primary` DESC, account.name COLLATE NOCASE" +
            ", identity.`primary` DESC, identity.display COLLATE NOCASE, identity.name COLLATE NOCASE, identity.email COLLATE NOCASE")
    List<TupleIdentityEx> getComposableIdentities(Long account);

    @Query("SELECT * FROM identity" +
            " WHERE account = :account" +
            " ORDER BY name COLLATE NOCASE")
    List<EntityIdentity> getIdentities(long account);

    @Query("SELECT * FROM identity" +
            " WHERE account = :account" +
            " AND email = :email COLLATE NOCASE")
    List<EntityIdentity> getIdentities(long account, String email);

    @Query("SELECT identity.* FROM identity" +
            " JOIN account ON account.id = identity.account" +
            " WHERE identity.account = :account" +
            " AND identity.synchronize AND account.synchronize" +
            " ORDER BY identity.`primary` DESC, IFNULL(identity.display, identity.name)")
    List<EntityIdentity> getSynchronizingIdentities(long account);

    @Query("SELECT COUNT(*) FROM identity WHERE synchronize")
    int getSynchronizingIdentityCount();

    @Query("SELECT * FROM identity WHERE id = :id")
    EntityIdentity getIdentity(long id);

    @Insert
    long insertIdentity(EntityIdentity identity);

    @Update
    void updateIdentity(EntityIdentity identity);

    @Query("UPDATE identity SET synchronize = :synchronize WHERE id = :id AND synchronize <> :synchronize")
    int setIdentitySynchronize(long id, boolean synchronize);

    @Query("UPDATE identity SET `primary` = :primary WHERE id = :id AND `primary` <> :primary")
    int setIdentityPrimary(long id, boolean primary);

    @Query("UPDATE identity SET state = :state WHERE id = :id AND state <> :state")
    int setIdentityState(long id, String state);

    @Query("UPDATE identity SET password = :password WHERE id = :id AND password <> :password")
    int setIdentityPassword(long id, String password);

    @Query("UPDATE identity SET password = :password" +
            " WHERE account = :account" +
            " AND user = :user" +
            " AND password <> :password" +
            " AND host LIKE :domain")
    int setIdentityPassword(long account, String user, String password, String domain);

    @Query("UPDATE identity SET password = :password" +
            " WHERE account = :account" +
            " AND user = :user" +
            " AND password <> :password" +
            " AND auth_type = :auth_type")
    int setIdentityPassword(long account, String user, String password, int auth_type);

    @Query("UPDATE identity SET last_connected = :last_connected WHERE id = :id AND last_connected <> :last_connected")
    int setIdentityConnected(long id, long last_connected);

    @Query("UPDATE identity SET encrypt = :encrypt WHERE id = :id AND encrypt <> :encrypt")
    int setIdentityEncrypt(long id, int encrypt);

    @Query("UPDATE identity SET sign_key = :sign_key WHERE id = :id AND sign_key <> :sign_key")
    int setIdentitySignKey(long id, Long sign_key);

    @Query("UPDATE identity SET sign_key_alias = :alias WHERE id = :id AND sign_key_alias <> :alias")
    int setIdentitySignKeyAlias(long id, String alias);

    @Query("UPDATE identity SET max_size = :max_size WHERE id = :id AND max_size <> :max_size")
    int setIdentityMaxSize(long id, Long max_size);

    @Query("UPDATE identity SET error = :error WHERE id = :id AND error <> :error")
    int setIdentityError(long id, String error);

    @Query("UPDATE identity SET `primary` = 0 WHERE account = :account AND `primary` <> 0")
    void resetPrimary(long account);

    @Query("DELETE FROM identity WHERE id = :id")
    int deleteIdentity(long id);
}
