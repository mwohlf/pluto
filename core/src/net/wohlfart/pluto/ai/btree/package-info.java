package net.wohlfart.pluto.ai.btree;

/*
 *
 * behavior:
 *
 * describes the logic relationship between tasks and contains static information like:
 * - execution order of tasks
 * - conditions for creating tasks
 *
 * has a link to the Executor context where a stack of current tasks is kept
 * an instance of a behaviour must be able to be reused for multiple entities
 *
 * a behaviour only knows its children never its parent this way the tree can be changed
 *
 *
 * task:
 * contains runtime information that is being changed
 * - describe the action that is being executed
 * - implements conditions
 *
 *
 *
 */
