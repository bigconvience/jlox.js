package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSMemoryUsage
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/221:58 PM
 */
public class JSMemoryUsage {
    long malloc_size, malloc_limit, memory_used_size;
    long malloc_count;
    long memory_used_count;
    long atom_count, atom_size;
    long str_count, str_size;
    long obj_count, obj_size;
    long prop_count, prop_size;
    long shape_count, shape_size;
    long js_func_count, js_func_size, js_func_code_size;
    long js_func_pc2line_count, js_func_pc2line_size;
    long c_func_count, array_count;
    long fast_array_count, fast_array_elements;
    long binary_object_count, binary_object_size;
}
