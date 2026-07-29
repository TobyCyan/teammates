package teammates.storage.api;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import teammates.common.util.HibernateUtil;
import teammates.storage.entity.MagicLink;

/**
 * Handles CRUD operations for magic links.
 *
 * @see MagicLink
 */
public final class MagicLinksDb {
    private static final MagicLinksDb instance = new MagicLinksDb();

    private MagicLinksDb() {
        // prevent initialization
    }

    public static MagicLinksDb inst() {
        return instance;
    }

    /**
     * Persists a MagicLink.
     */
    public MagicLink persistMagicLink(MagicLink magicLink) {
        HibernateUtil.persist(magicLink);
        return magicLink;
    }

    /**
     * Returns a MagicLink with the given token hash or null if it does not exist.
     */
    public MagicLink getMagicLinkByTokenHash(String tokenHash) {
        CriteriaBuilder cb = HibernateUtil.getCriteriaBuilder();
        CriteriaQuery<MagicLink> cr = cb.createQuery(MagicLink.class);
        Root<MagicLink> root = cr.from(MagicLink.class);

        cr.select(root).where(cb.equal(root.get("tokenHash"), tokenHash));

        return HibernateUtil.createQuery(cr).getResultStream().findFirst().orElse(null);
    }

    /**
     * Deletes a MagicLink.
     */
    public void deleteMagicLink(MagicLink magicLink) {
        HibernateUtil.remove(magicLink);
    }

}
