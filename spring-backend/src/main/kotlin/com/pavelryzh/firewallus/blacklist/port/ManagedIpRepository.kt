package com.pavelryzh.firewallus.blacklist.port

import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.domain.ManagedIp
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ManagedIpRepository : JpaRepository<ManagedIp, UUID> {

    /**
     * Возвращает страницу IP-адресов, отфильтрованную по типу списка (BLACKLIST/WHITELIST).
     *
     * [Under the hood (Spring Data JPA generates 2 queries)]:
     * 1. HQL: SELECT count(m) FROM ManagedIp m WHERE m.listType = :listType
     *    SQL: SELECT count(id) FROM ip_lists WHERE list_type = ?;
     *
     * 2. HQL: SELECT m FROM ManagedIp m WHERE m.listType = :listType
     *    SQL: SELECT * FROM ip_lists WHERE list_type = ? LIMIT ? OFFSET ?;
     */

    fun findByListType(listType: IpListType, pageable: Pageable): Page<ManagedIp>

    /**
     * Ищет конкретный IP-адрес в списках доступа.
     *
     * [Under the hood]:
     * HQL: SELECT m FROM ManagedIp m WHERE m.ipAddress = :address
     * SQL: SELECT id, ip_address, list_type, description, created_at FROM ip_lists WHERE ip_address = ?;
     */

    fun findByIpAddress(address: String): ManagedIp?

    @Modifying
    @Query("DELETE FROM ManagedIp ip WHERE ip.ipAddress = :address")
    fun deleteAllByIpAddress(address: String)
}
