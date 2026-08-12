/* Taken from WildAnimalsPlus by jabelar */
/*
 * https://github.com/jabelar/WildAnimalsPlus-1.7.10/blob/master/src/main/java/com/blogspot/jabelarminecraft/wildanimals
 * /entities/IModEntity.java
 */
/* License: GPLv3 - <http://www.gnu.org/licenses/> */

package org.fentanylsolutions.eyesintheshadows.entity;

public interface IModEntity {

    // set up AI tasks
    void setupAI();

    // use clear tasks for subclasses then build up their ai task list specifically
    void clearAITasks();

    // common encapsulation methods
    void setScaleFactor(float parScaleFactor);

    float getScaleFactor();
}
