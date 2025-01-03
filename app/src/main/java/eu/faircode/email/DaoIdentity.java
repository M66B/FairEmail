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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
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

    @Query("SELECT identity.*" +
            ", account.name AS accountName, account.category AS accountCategory, account.color AS accountColor, account.synchronize AS accountSynchronize" +
            ", folder.id AS drafts" +
            " FROM identity" +
            " JOIN account ON account.id = identity.account" +
            " LEFT JOIN folder ON folder.account = account.id AND folder.type = '" + EntityFolder.DRAFTS + "'")
    LiveData<List<TupleIdentityEx>> liveIdentities();

    @Query("SELECT identity.*" +
            ", account.name AS accountName, account.category AS accountCategory, account.color AS accountColor, account.synchronize AS accountSynchronize" +
            ", folder.id AS drafts" +
            " FROM identity" +
            " JOIN account ON account.id = identity.account" +
            " JOIN folder ON folder.account = identity.account AND folder.type = '" + EntityFolder.DRAFTS + "'" +
            " AND identity.synchronize" +
            " AND account.synchronize")
    LiveData<List<TupleIdentityEx>> liveComposableIdentities();

    @Query("SELECT identity.*" +
            ", account.name AS accountName, account.category AS accountCategory, account.color AS accountColor, account.synchronize AS accountSynchronize" +
            ", folder.id AS drafts" +
            " FROM identity" +
            " JOIN account ON account.id = identity.account" +
            " JOIN folder ON folder.account = identity.account AND folder.type = '" + EntityFolder.DRAFTS + "'" +
            " WHERE (:account IS NULL OR account.id = :account)" +
            " AND identity.synchronize" +
            " AND account.synchronize" +
            " ORDER BY account.`primary` DESC, account.`order`, account.name COLLATE NOCASE" +
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

    @Query("SELECT identity.* FROM identity" +
            " JOIN account ON account.id = identity.account" +
            " JOIN folder ON folder.account = identity.account AND folder.type = '" + EntityFolder.DRAFTS + "'" +
            " WHERE account.`primary` AND account.synchronize" +
            " AND identity.`primary` AND identity.synchronize")
    EntityIdentity getPrimaryIdentity();

    @Query("SELECT * FROM identity WHERE uuid = :uuid")
    EntityIdentity getIdentityByUUID(String uuid);

    @Query("SELECT * FROM identity WHERE display = :display")
    List<EntityIdentity> getIdentityByDisplayName(String display);

    @Insert
    long insertIdentity(EntityIdentity identity);

    @Update
    void updateIdentity(EntityIdentity identity);

    @Query("UPDATE identity SET uuid = :uuid WHERE id = :id AND NOT (uuid IS :uuid)")
    int setIdentityUuid(long id, String uuid);

    @Query("UPDATE identity SET synchronize = :synchronize WHERE id = :id AND NOT (synchronize IS :synchronize)")
    int setIdentitySynchronize(long id, boolean synchronize);

    @Query("UPDATE identity SET `primary` = :primary WHERE id = :id AND NOT (`primary` IS :primary)")
    int setIdentityPrimary(long id, boolean primary);

    @Query("UPDATE identity SET color = :color WHERE id = :id AND NOT (color IS :color)")
    int setIdentityColor(long id, Integer color);

    @Query("UPDATE identity SET state = :state WHERE id = :id AND NOT (state IS :state)")
    int setIdentityState(long id, String state);

    @Query("UPDATE identity SET password = :password WHERE id = :id AND NOT (password IS :password)")
    int setIdentityPassword(long id, String password);

    @Query("UPDATE identity" +
            " SET password = :password, auth_type = :auth_type" +
            " WHERE account = :account" +
            " AND user = :user" +
            " AND NOT (password IS :password AND auth_type = :auth_type)" +
            " AND host LIKE :domain")
    int setIdentityPassword(long account, String user, String password, int auth_type, String domain);

    @Query("UPDATE identity" +
            " SET password = :password, auth_type = :new_auth_type, provider = :provider" +
            " WHERE account = :account" +
            " AND user = :user" +
            " AND (auth_type = :auth_type OR :auth_type IS NULL)" +
            " AND NOT (password IS :password AND auth_type IS :new_auth_type AND provider = :provider)")
    int setIdentityPassword(long account, String user, String password, Integer auth_type, int new_auth_type, String provider);

    @Query("UPDATE identity" +
            " SET fingerprint = :fingerprint, insecure = :insecure" +
            " WHERE account = :account" +
            " AND NOT (fingerprint IS :fingerprint)")
    int setIdentityFingerprint(long account, String fingerprint, boolean insecure);

    @Query("UPDATE identity SET last_connected = :last_connected WHERE id = :id AND NOT (last_connected IS :last_connected)")
    int setIdentityConnected(long id, long last_connected);

    @Query("UPDATE identity SET encrypt = :encrypt WHERE id = :id AND NOT (encrypt IS :encrypt)")
    int setIdentityEncrypt(long id, int encrypt);

    @Query("UPDATE identity SET sign_default = 0, encrypt_default = 0 WHERE encrypt = 0")
    int resetIdentityPGP();

    @Query("UPDATE identity SET sign_key = :sign_key WHERE id = :id AND NOT (sign_key IS :sign_key)")
    int setIdentitySignKey(long id, Long sign_key);

    @Query("UPDATE identity SET sign_key_alias = :alias WHERE id = :id AND NOT (sign_key_alias IS :alias)")
    int setIdentitySignKeyAlias(long id, String alias);

    @Query("UPDATE identity SET sign_key_alias = NULL")
    int clearIdentitySignKeyAliases();

    @Query("UPDATE identity SET max_size = :max_size WHERE id = :id AND NOT (max_size IS :max_size)")
    int setIdentityMaxSize(long id, Long max_size);

    @Query("UPDATE identity SET signature = :hmtl WHERE id = :id AND NOT (signature IS :hmtl)")
    int setIdentitySignature(long id, String hmtl);

    @Query("UPDATE identity SET error = :error WHERE id = :id AND NOT (error IS :error)")
    int setIdentityError(long id, String error);

    @Query("UPDATE identity SET `primary` = 0 WHERE account = :account AND NOT (`primary` IS 0)")
    void resetPrimary(long account);

    @Query("UPDATE identity" +
            " SET last_modified = :last_modified" +
            " WHERE id = :id")
    int setIdentityLastModified(long id, Long last_modified);

    @Query("DELETE FROM identity WHERE id = :id")
    int deleteIdentity(long id);
}
