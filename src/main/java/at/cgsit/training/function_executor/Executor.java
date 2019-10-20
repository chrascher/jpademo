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
package at.cgsit.training.function_executor;

import at.cgsit.training.UserAccount;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;
import java.util.function.BiFunction;

/**
 *
 */
public class Executor {
    // Create an EntityManagerFactory when you start the application.
    private static final EntityManagerFactory EM_FACTORY = Persistence.createEntityManagerFactory("chatsPU");

    /**
     * reusable method taking a function to call after preparing transactions,
     * and commiting after calling the function
     */
    public UserAccount execute(UserAccount ua, BiFunction<UserAccount, EntityManager, UserAccount> fkt) {

        EntityManager manager = EM_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        UserAccount obj = null;

        try {
            transaction = manager.getTransaction();
            transaction.begin();

            // apply/call the function to perform the actual work to do
            obj = fkt.apply(ua, manager);

            transaction.commit();
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        } finally {
            manager.close();
        }
        return obj;
    }


    /**
     * reusable method taking a function to call after preparing transactions,
     * and commiting after calling the function
     */
    public Object executeObj(Object input, BiFunction<Object, EntityManager, Object> fkt) {

        EntityManager manager = EM_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        Object obj = null;

        try {
            transaction = manager.getTransaction();
            transaction.begin();

            // apply/call the function to perform the actual work to do
            obj = (Object)fkt.apply(input, manager);

            transaction.commit();
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        } finally {
            manager.close();
        }
        return obj;
    }

    /**
     * reusable method taking a function to call after preparing transactions,
     * and commiting after calling the function
     */
    public List<UserAccount> executeListResult(String input, BiFunction<String, EntityManager, List<UserAccount>> fkt) {

        EntityManager manager = EM_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        List<UserAccount> resList = null;

        try {
            transaction = manager.getTransaction();
            transaction.begin();

            // apply/call the function to perform the actual work to do
            resList = fkt.apply(input, manager);

            if (resList != null)
                resList.stream().forEach(item -> System.out.println(item));

            transaction.commit();
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        } finally {
            manager.close();
        }
        return resList;
    }

}
