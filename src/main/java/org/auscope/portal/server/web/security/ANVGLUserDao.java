package org.auscope.portal.server.web.security;

import java.util.List;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * A data access object for VEGLJob
 * @author Josh Vote
 *
 */
//@Transactional
public class ANVGLUserDao extends HibernateDaoSupport {
    /**
     * Retrieves ANVGLUser that has the specified UD
     *
     * @param id the ID of the user
     */
	@Transactional
    public ANVGLUser getById(String id) {
        return getHibernateTemplate().get(ANVGLUser.class, id);
    }

    /**
     * Retrieves ANVGLUser that has the specified UD
     *
     * @param id the ID of the user
     */
	@Transactional
    public ANVGLUser getByEmail(String email) {
        List<?> resList = getHibernateTemplate().findByNamedParam("from ANVGLUser u where u.email =:p", "p", email);
        if(resList.isEmpty()) return null;
        return (ANVGLUser) resList.get(0);
    }
    
    /**
     * Deletes the given user.
     */
	@Transactional
    public void deleteUser(ANVGLUser user) {
        getHibernateTemplate().delete(user);
    }

    /**
     * Saves or updates the given user.
     */
	@Transactional
    public void save(ANVGLUser user) {
        getHibernateTemplate().saveOrUpdate(user);
    }
}
