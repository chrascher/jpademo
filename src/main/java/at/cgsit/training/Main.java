/*
 * (C) Copyright 22019 CGS IT-Solutions (http://cgs.at/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package at.cgsit.training;

import at.cgsit.training.function_executor.Executor;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * demo for using jpa and hibernate without any JEE or Servlet Container
 * <br/>
 * To use this exmples please create the simpledb database, schema, users
 * and User Account table defined in src/main/sql
 * <p>
 * JPQL siehe : Query query = manage
 * https://en.wikibooks.org/wiki/Java_Persistence/JPQL
 *
 * @Author CGS-IT Solutions @2019
 */
public class Main {
    // Create an EntityManagerFactory when you start the application.
    private static final EntityManagerFactory EM_FACTORY = Persistence.createEntityManagerFactory("chatsPU");

    private final Executor executor = new Executor();

    /**
     * entry point
     */
    public static void main(String[] args) {

        Main myMain = new Main();
        myMain.doWork();
    }

    private void doWork() {
        try {

            //
            // 1. example reading (all)s entries with named query
            //
            System.out.println("inital users: ");
            executor.executeListResult("", this::readAllWithNamedQuery);

            System.out.println("criteria query: ");
            executor.executeListResult("", this::findWithCriteriaBuilder);

            System.out.println("named query with query parameter: ");
            executor.executeListResult("", this::findAllWithNamedQuery);

            System.out.println("use count all JPQL: ");
            executor.executeObj("", this::useAggregateFunctions);

            System.out.println("use native sql select query: ");
            executor.executeObj("", this::findBySQLNativeQuery);

            //
            // 2. use simple insert with full sequence of todos to insert into the db
            //
            UserAccount ua1 = new UserAccount();
            ua1.setPassword("12345");
            this.insertWithPreAndPostWork(ua1);

            //
            // 3. execute an insert statement with executor simplification
            //
            UserAccount ua = new UserAccount();
            ua.setPassword("12345");

            executor.execute(ua, this::insertExample);

            //
            // update
            // 
            ua.setUsername(ua.getUsername() + ua.getId() + 1);
            ua.setEmail("test.modified" + ua.getId() + 1 + "@cgs.at");

            executor.execute(ua, this::update);

            List<UserAccount> afterUpd = executor.executeListResult("", this::readAllWithNamedQuery);

            System.out.println("after update: ");
            logAccountsToConsole(afterUpd);

            //
            // delete an object
            //
            final Boolean result = (Boolean)
                    executor.executeObj(5L, this::delete);

            System.out.println("after delete: ");
            executor.executeListResult("", this::readAllWithNamedQuery);

        } finally {
            EM_FACTORY.close();
        }
    }

    private static void logAccountsToConsole(List<UserAccount> accounts) {

        if (accounts != null)
            accounts.stream().forEach(item -> System.out.println(item));

        /* looping classically .. ;-)
        if (accounts == null)
            return;
        for (UserAccount acc : accounts) {
            System.out.println(acc);
        }
         */
    }


    public UserAccount insertExample(UserAccount ua, EntityManager manager) {

        // if there is no automatic sequence for the entity like there
        // we just create the next valid id for our manual sequence by selecting
        // the current max id
        // this might lead to exceptions in concurrent use-cases
        // use automatically generated ids instead or use UUIDs
        Query query = manager.createQuery("SELECT max(acc.id) FROM UserAccount acc");
        Integer count = (Integer) query.getSingleResult();
        System.out.println("max id: " + count);

        ua.setId(count + 1);
        // if the email and username are unique, we try to make sure those are also uniques
        ua.setEmail("test" + ua.getId() + "@cgs.at");
        ua.setUsername("test" + ua.getId());

        // now persist the entity using the entity Managers persist method s
        manager.persist(ua);

        return ua;
    }

    /**
     * this example just avoids the executor to show the full sequence of methods for
     * using an entity Manager.
     *
     * @param ua
     * @return UserAccount the inserted user account (detached)
     */
    public UserAccount insertWithPreAndPostWork(UserAccount ua) {

        EntityManager manager = EM_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try {
            transaction = manager.getTransaction();
            transaction.begin();

            Query query = manager.createQuery("SELECT max(acc.id) FROM UserAccount acc");
            Integer count = (Integer) query.getSingleResult();
            System.out.println("max id: " + count);

            ua.setId(count + 1);

            ua.setEmail("test" + ua.getId() + "@cgs.at");
            ua.setUsername("test" + ua.getId());

            manager.persist(ua);

            transaction.commit();
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        } finally {
            manager.close();
        }

        return ua;
    }

    /**
     * delete the entity with Id entityId
     *
     * @param inObj input should be an id ass Long value
     * @return Boolean.TRUE
     */
    public Object delete(Object inObj, EntityManager manager) {

        Long entityId = (Long) inObj;
        UserAccount accountDb = manager.find(UserAccount.class, entityId.intValue());
        if (accountDb == null) {
            throw new RuntimeException("account not found");
        }

        manager.remove(accountDb);

        return Boolean.TRUE;
    }

    /**
     * use aggregate count function
     *
     * @param inObj   unused
     * @param manager EntityManager
     * @return count result
     */
    public Object useAggregateFunctions(Object inObj, EntityManager manager) {

        Query query = manager.createQuery("SELECT COUNT(acc) FROM UserAccount acc");

        Long count = (Long) query.getSingleResult();

        System.out.println("count: " + count);

        return (Object) count;
    }

    public Object findBySQLNativeQuery(Object inObj, EntityManager manager) {
        Query q =
                manager.createNativeQuery("SELECT username, user_id from chats.account");

        List<Object[]> accountsNativeList = q.getResultList();
        for (Object[] a : accountsNativeList) {
            System.out.println("Account " + a[0] + " " + a[1]);
        }

        return Boolean.TRUE;
    }


    /**
     * example update
     *
     * @param uaInput
     * @param manager
     * @return
     */
    public UserAccount update(UserAccount uaInput, EntityManager manager) {

        UserAccount accountDb = manager.find(UserAccount.class, uaInput.getId());
        if (accountDb == null) {
            throw new RuntimeException("account not found");
        }

        accountDb.setEmail(uaInput.getEmail());
        accountDb.setPassword(uaInput.getPassword());
        accountDb.setUsername(uaInput.getUsername());

        // since this entity is already attached/Persistent
        // changes will be committed automatically to the db during commit already

        // clarify with your team first about what kind of update strategy will be used
        // in your project

        return accountDb;
    }

    /**
     * read all entities by using a named query
     *
     * @param parameter not used
     * @param manager   the em to use
     * @return List<UserAccount>
     */
    public List<UserAccount> readAllWithNamedQuery(String parameter, EntityManager manager) {
        List<UserAccount> accounts = null;

        // no query parameter is used -- parameter
        Query query = manager.createNamedQuery("UserAccount.findAllOrderedByName", UserAccount.class);
        accounts = query.getResultList();

        return accounts;
    }

    /**
     * findAllWithNamedQuery
     */
    public List<UserAccount> findAllWithNamedQuery(String parameter, EntityManager manager) {
        List<UserAccount> accounts = null;
        Query query = manager.createNamedQuery("UserAccount.findByEmail", UserAccount.class);
        // use the namd parameter and find all users with email starting with j and ending with .at
        query.setParameter("email", "j%.at");
        accounts = query.getResultList();

        return accounts;
    }


    /**
     * findWithCriteriaBuilder
     */
    public List<UserAccount> findWithCriteriaBuilder(String parameter, EntityManager manager) {

        List<UserAccount> accounts = null;

        CriteriaBuilder cb = manager.getCriteriaBuilder();

        CriteriaQuery<UserAccount> cq = cb.createQuery(UserAccount.class);
        Root<UserAccount> from = cq.from(UserAccount.class);
        cq.select(from);

        /// WHERE ( (email = x) OR (email = y) )
        // Predicate equals = cb.equal( from.get("username"), "john");

        Predicate like = cb.like(from.get("email"), "j%.at");
        Predicate like2 = cb.like(from.get("email"), "m%.at");

        Predicate or = cb.or(like, like2);

        Predicate gt2 = cb.ge(from.get("id"), 2);

        Predicate and = cb.and(gt2, or);

        cq.where(and);

        Query query = manager.createQuery(cq);
        accounts = query.getResultList();

        return accounts;
    }

}

