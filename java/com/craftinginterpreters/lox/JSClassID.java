package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: ClassID
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/31:48 PM
 */
public enum JSClassID {
  /* classid tag        */    /* union usage   | properties */
  JS_CLASS_OBJECT,        /* must be first */
  JS_CLASS_ARRAY,             /* u.array       | length */
  JS_CLASS_ERROR,
  JS_CLASS_NUMBER,            /* u.object_data */
  JS_CLASS_STRING,            /* u.object_data */
  JS_CLASS_BOOLEAN,           /* u.object_data */
  JS_CLASS_SYMBOL,            /* u.object_data */
  JS_CLASS_ARGUMENTS,         /* u.array       | length */
  JS_CLASS_MAPPED_ARGUMENTS,  /*               | length */
  JS_CLASS_DATE,              /* u.object_data */
  JS_CLASS_MODULE_NS,
  JS_CLASS_C_FUNCTION,        /* u.cfunc */
  JS_CLASS_BYTECODE_FUNCTION, /* u.func */
  JS_CLASS_BOUND_FUNCTION,    /* u.bound_function */
  JS_CLASS_C_FUNCTION_DATA,   /* u.c_function_data_record */
  JS_CLASS_GENERATOR_FUNCTION, /* u.func */
  JS_CLASS_FOR_IN_ITERATOR,   /* u.for_in_iterator */
  JS_CLASS_REGEXP,            /* u.regexp */
  JS_CLASS_ARRAY_BUFFER,      /* u.array_buffer */
  JS_CLASS_SHARED_ARRAY_BUFFER, /* u.array_buffer */
  JS_CLASS_UINT8C_ARRAY,      /* u.array (typed_array) */
  JS_CLASS_INT8_ARRAY,        /* u.array (typed_array) */
  JS_CLASS_UINT8_ARRAY,       /* u.array (typed_array) */
  JS_CLASS_INT16_ARRAY,       /* u.array (typed_array) */
  JS_CLASS_UINT16_ARRAY,      /* u.array (typed_array) */
  JS_CLASS_INT32_ARRAY,       /* u.array (typed_array) */
  JS_CLASS_UINT32_ARRAY,      /* u.array (typed_array) */

  JS_CLASS_FLOAT32_ARRAY,     /* u.array (typed_array) */
  JS_CLASS_FLOAT64_ARRAY,     /* u.array (typed_array) */
  JS_CLASS_DATAVIEW,          /* u.typed_array */

  JS_CLASS_MAP,               /* u.map_state */
  JS_CLASS_SET,               /* u.map_state */
  JS_CLASS_WEAKMAP,           /* u.map_state */
  JS_CLASS_WEAKSET,           /* u.map_state */
  JS_CLASS_MAP_ITERATOR,      /* u.map_iterator_data */
  JS_CLASS_SET_ITERATOR,      /* u.map_iterator_data */
  JS_CLASS_ARRAY_ITERATOR,    /* u.array_iterator_data */
  JS_CLASS_STRING_ITERATOR,   /* u.array_iterator_data */
  JS_CLASS_REGEXP_STRING_ITERATOR,   /* u.regexp_string_iterator_data */
  JS_CLASS_GENERATOR,         /* u.generator_data */
  JS_CLASS_PROXY,             /* u.proxy_data */
  JS_CLASS_PROMISE,           /* u.promise_data */
  JS_CLASS_PROMISE_RESOLVE_FUNCTION,  /* u.promise_function_data */
  JS_CLASS_PROMISE_REJECT_FUNCTION,   /* u.promise_function_data */
  JS_CLASS_ASYNC_FUNCTION,            /* u.func */
  JS_CLASS_ASYNC_FUNCTION_RESOLVE,    /* u.async_function_data */
  JS_CLASS_ASYNC_FUNCTION_REJECT,     /* u.async_function_data */
  JS_CLASS_ASYNC_FROM_SYNC_ITERATOR,  /* u.async_from_sync_iterator_data */
  JS_CLASS_ASYNC_GENERATOR_FUNCTION,  /* u.func */
  JS_CLASS_ASYNC_GENERATOR,   /* u.async_generator_data */

  JS_CLASS_INIT_COUNT, /* last entry for predefined classes */
}
