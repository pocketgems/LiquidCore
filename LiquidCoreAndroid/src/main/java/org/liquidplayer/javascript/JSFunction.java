/*
 * Copyright (c) 2014 - 2018 Eric Lange
 *
 * Distributed under the MIT License.  See LICENSE.md at
 * https://github.com/LiquidPlayer/LiquidCore for terms and conditions.
 */
package org.liquidplayer.javascript;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * A JavaScript function object.
 * @since 0.1.0
 *
 */
public class JSFunction extends JSObject {

    /**
     * Creates a JavaScript function that takes parameters 'parameterNames' and executes the
     * JS code in 'body'.
     *
     * @param ctx                The JSContext in which to create the function
     * @param name               The name of the function
     * @param parameterNames     A String array containing the names of the parameters
     * @param body               The JavaScript code to execute in the function
     * @param sourceURL          The URI of the source file, only used for reporting in stack trace (optional)
     * @param startingLineNumber The beginning line number, only used for reporting in stack trace (optional)
     * @since 0.1.0
     */
    public JSFunction(JSContext ctx, final @NonNull String name, final @NonNull String[] parameterNames,
                      final @NonNull String body, final String sourceURL, final int startingLineNumber)
    {
        context = ctx;
        StringBuilder func = new StringBuilder("(function(");
        for (int i=0; i<parameterNames.length; i++) {
            func.append(parameterNames[i]);
            if (i<parameterNames.length-1) {
                func.append(",");
            }
        }
        func.append("){");
        func.append(body);
        func.append("})");
        final String function = func.toString();

        try {
            valueRef = context.ctxRef().makeFunction(
                    name,
                    function,
                    (sourceURL == null) ? "<anonymous>" : sourceURL,
                    startingLineNumber);
        } catch (JNIJSException excp) {
            valueRef = testException(excp.exception);
        }

        addJSExports();
        context.persistObject(this);
    }

    /**
     * Creates a JavaScript function that takes parameters 'parameterNames' and executes the
     * JS code in 'body'.
     *
     * @param ctx                The JSContext in which to create the function
     * @param name               The name of the function
     * @param parameterNames     A String array containing the names of the parameters
     * @param body               The JavaScript code to execute in the function
     * @since 0.1.0
     */
    public JSFunction(JSContext ctx, final @NonNull String name, final @NonNull String body,
                      final String ... parameterNames)
    {
        this(ctx,name,parameterNames,body,null,1);
    }

    private JNIJSObject testException(@NonNull JNIJSValue exception) {
        context.throwJSException(new JSException(new JSValue(exception, context)));
        return context.ctxRef().make();
    }

    /**
     * Creates a new function object which calls method 'method' on this Java object.
     * Assumes the 'method' exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new 'instanceClass' instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     *
     * public class FunctionObject extends JSObject {
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     *
     * public class MyFunc extends JSFunction {
     *     MyFunc(JSContext ctx) {
     *         super(ctx,
     *              FunctionObject.class.getMethod("function",int.class), // will call method 'function'
     *              JSObject.class                                // calling 'new' will create a JSObject
     *              new FunctionObject(ctx)                       // function will be called on FunctionObject
     *         );
     *     }
     * }
     * }
     * </pre>
     *
     * @param ctx    The JSContext to create the object in
     * @param method The method to invoke
     * @param instanceClass The class to be created on 'new' call
     * @param invokeObject  The object on which to invoke the method
     * @since 0.1.0
     */
    public JSFunction(JSContext ctx,
                      final Method method,
                      final Class<? extends JSObject> instanceClass,
                      JSObject invokeObject) {
        context = ctx;
        this.method = method;
        this.method.setAccessible(true);
        this.pType  = this.method.getParameterTypes();
        this.invokeObject = (invokeObject==null) ? this: invokeObject;
        valueRef = context.ctxRef().makeFunctionWithCallback(JSFunction.this, method.getName());
        subclass = instanceClass;
        addJSExports();
        context.persistObject(this);
        context.zombies.add(this);
    }
    /**
     * Creates a new function object which calls method 'method' on this Java object.
     * Assumes the 'method' exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new 'instanceClass' instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     * public class MyFunc extends JSFunction {
     *     MyFunc(JSContext ctx) {
     *         super(ctx,
     *              MyFunc.class.getMethod("function",int.class), // will call method 'function'
     *              JSObject.class                                // calling 'new' will create a JSObject
     *         );
     *     }
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     * }
     * </pre>
     *
     *
     * @param ctx    The JSContext to create the object in
     * @param method The method to invoke
     * @param instanceClass The class to be created on 'new' call
     * @since 0.1.0
     */
    public JSFunction(JSContext ctx,
                      final Method method,
                      final Class<? extends JSObject> instanceClass) {
        this(ctx,method,instanceClass,null);
    }
    /**
     * Creates a new function object which calls method 'method' on this Java object.
     * Assumes the 'method' exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new JSObject instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     * public class MyFunc extends JSFunction {
     *     MyFunc(JSContext ctx) {
     *         super(ctx,
     *              MyFunc.class.getMethod("function",int.class), // will call method 'function'
     *              JSObject.class                                // calling 'new' will create a JSObject
     *         );
     *     }
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     * }
     * </pre>
     *
     *
     * @param ctx    The JSContext to create the object in
     * @param method The method to invoke
     * @since 0.1.0
     */
    public JSFunction(JSContext ctx,
                      final Method method) {
        this(ctx,method,JSObject.class);
    }
    /**
     * Creates a new function which basically does nothing.
     * In JS:
     * <pre>{@code
     * var f = function() {};
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     * JSFunction f = new JSFunction(context);
     * }
     * </pre>
     *
     *
     * @param ctx    The JSContext to create the object in
     * @since 0.1.0
     */
    public JSFunction(JSContext ctx) {
        this(ctx,(String)null);
    }

    /**
     * Creates a new function object which calls method 'methodName' on this Java object.
     * Assumes the 'methodName' method exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new 'instanceClass' instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     *
     * public class FunctionObject extends JSObject {
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     *
     * public class MyFunc extends JSFunction {
     *     MyFunc(JSContext ctx) {
     *         super(ctx,
     *              "function",               // will call method 'function'
     *              JSObject.class            // calling 'new' will create a JSObject
     *              new FunctionObject(ctx)   // function will be called on FunctionObject
     *         );
     *     }
     * }
     * }
     * </pre>
     *
     * @param ctx    The JSContext to create the object in
     * @param methodName The method to invoke (searches for first instance)
     * @param instanceClass The class to be created on 'new' call
     * @param invokeObject  The object on which to invoke the method
     * @since 0.1.0
     */
    public JSFunction(JSContext ctx,
                      final String methodName,
                      final Class<? extends JSObject> instanceClass,
                      JSObject invokeObject) {
        context = ctx;
        this.invokeObject = (invokeObject==null) ? this : invokeObject;
        String name = (methodName==null) ? "__nullFunc" : methodName;
        Method [] methods = this.invokeObject.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                this.method = method;
                break;
            }
        }
        if (method == null) {
            valueRef = context.ctxRef().makeUndefined();
            context.throwJSException(new JSException(context,"No such method. Did you make it public?"));
        } else {
            this.method.setAccessible(true);
            this.pType = this.method.getParameterTypes();
            valueRef = context.ctxRef().makeFunctionWithCallback(JSFunction.this, method.getName());
            subclass = instanceClass;
            addJSExports();
        }

        context.persistObject(this);
        context.zombies.add(this);
    }
    /**
     * Creates a new function object which calls method 'methodName' on this Java object.
     * Assumes the 'methodName' method exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new JSObject instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     *
     * JSFunction f = new JSFunction(context,"function",JSObject.class) {
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     * }
     * </pre>
     *
     * @param ctx    The JSContext to create the object in
     * @param methodName The method to invoke (searches for first instance)
     * @param instanceClass The class to be created on 'new' call
     * @since 0.1.0
     */
    public JSFunction(JSContext ctx,
                      final String methodName,
                      final Class<? extends JSObject> instanceClass) {
        this(ctx,methodName,instanceClass,null);
    }
    /**
     * Creates a new function object which calls method 'methodName' on this Java object.
     * Assumes the 'methodName' method exists on this object and will throw a JSException if not found.  If
     * 'new' is called on this function, it will create a new JSObject instance.
     * In JS:
     * <pre>{@code
     * var f = function(a) { ... };
     * }
     * </pre>
     *
     * Example:
     * <pre>{@code
     *
     * JSFunction f = new JSFunction(context,"function") {
     *     void function(int x) {
     *         getThis().property("varx",x);
     *     }
     * }
     * }
     * </pre>
     *
     * @param ctx    The JSContext to create the object in
     * @param methodName The method to invoke (searches for first instance)
     * @since 0.1.0
     */
    public JSFunction(JSContext ctx,
                      final String methodName) {
        this(ctx,methodName,JSObject.class);
    }

    /**
     * Wraps an existing object as a JSFunction
     * @param objRef  The JavaScriptCore object reference
     * @param context The JSContext the object
     * @since 0.1.0
     */
    public JSFunction(final JNIJSObject objRef, JSContext context) {
        super(objRef, context);
    }

    /**
     * Calls this JavaScript function, similar to 'Function.call()' in JavaScript
     * @param thiz  The 'this' object on which the function operates, null if not on a constructor object
     * @param args  The argument list to be passed to the function
     * @return The JSValue returned by the function
     * @since 0.1.0
     */
    public JSValue call(final JSObject thiz, final Object ... args) {
        return apply(thiz,args);
    }

    private JNIJSValue [] argsToValueRefs(final Object[] args) {
        ArrayList<JSValue> largs = new ArrayList<>();
        if (args!=null) {
            for (Object o: args) {
                JSValue v;
                if (o == null) break;
                if (o.getClass() == Void.class)
                    v = new JSValue(context);
                else if (o instanceof JSValue)
                    v = (JSValue)o;
                else if (o instanceof Object[])
                    v = new JSArray<>(context, (Object[])o, Object.class);
                else
                    v = new JSValue(context,o);
                largs.add(v);
            }
        }
        JNIJSValue [] valueRefs = new JNIJSValue[largs.size()];
        for (int i=0; i<largs.size(); i++) {
            valueRefs[i] = largs.get(i).valueRef();
        }
        return valueRefs;
    }

    /**
     * Calls this JavaScript function, similar to 'Function.apply() in JavaScript
     * @param thiz  The 'this' object on which the function operates, null if not on a constructor object
     * @param args  An array of arguments to be passed to the function
     * @return The JSValue returned by the function
     * @since 0.1.0
     */
    public JSValue apply(final JSObject thiz, final Object [] args) {
        try {
            return new JSValue(JNI().callAsFunction(
                    (thiz == null) ? null : thiz.JNI(), argsToValueRefs(args)), context);
        } catch (JNIJSException excp) {
            context.throwJSException(
                    new JSException(new JSValue(excp.exception,context)));
            return new JSValue(context);
        }
    }
    /**
     * Calls this JavaScript function with no args and 'this' as null
     * @return The JSValue returned by the function
     * @since 0.1.0
     */
    public JSValue call() {
        return call(null);
    }

    /**
     * Calls this JavaScript function as a constructor, i.e. same as calling 'new func(args)'
     * @param args The argument list to be passed to the function
     * @return an instance object of the constructor
     * @since 0.1.0
     */
    public JSObject newInstance(final Object ... args) {
        try {
            return context.getObjectFromRef(JNI().callAsConstructor(argsToValueRefs(args)));
        } catch (JNIJSException excp) {
            return new JSObject(testException(excp.exception), context);
        }
    }

    @SuppressWarnings("unused") // This is called directly from native code
    @Keep
    private long functionCallback(long thisObjectRef, long[] argumentsValueRef) {
        long reference = JNIJSValue.ODDBALL_UNDEFINED;
        try {
            JSValue [] args = new JSValue[argumentsValueRef.length];
            for (int i=0; i<argumentsValueRef.length; i++) {
                JNIJSValue ref = JNIJSValue.fromRef(argumentsValueRef[i]);
                if (ref.isObject()) {
                    try {
                        args[i] = context.getObjectFromRef(ref.toObject());
                    } catch (JNIJSException e) {
                        e.printStackTrace();
                        throw new AssertionError();
                    }
                } else {
                    args[i] = new JSValue(ref,context);
                }
            }
            JNIJSObject thizRef = (!JNIJSValue.isReferenceObject(thisObjectRef)) ? null :
                    JNIJSObject.fromRef(thisObjectRef);
            JSObject thiz = thizRef == null ? null : context.getObjectFromRef(thizRef);
            JSValue value = function(thiz,args,invokeObject);
            reference = value==null ? JNIJSValue.ODDBALL_UNDEFINED: value.valueRef().reference;
        } catch (JSException e) {
            e.printStackTrace();
            JNIJSFunction.setException(valueRef().reference, e.getError().valueRef().reference);
        }
        return reference;
    }

    protected JSValue function(JSObject thiz, JSValue [] args) {
        return function(thiz,args,this);
    }

    protected JSValue function(JSObject thiz, JSValue [] args, final JSObject invokeObject) {
        Object [] passArgs = new Object[pType.length];
        if (pType.length > 0 && args.length >= pType.length && !args[args.length -1].isArray() &&
                pType[pType.length - 1].isArray()) {
            int i = 0;
            for (; i < passArgs.length - 1; i++) {
                if (i < args.length) {
                    if (args[i] == null) passArgs[i] = null;
                    else passArgs[i] = args[i].toJavaObject(pType[i]);
                } else {
                    passArgs[i] = null;
                }
            }

            JSValue[] varArgs = new JSValue[args.length - pType.length + 1];
            int varArgsCounter = 0;
            for (; i < args.length; ++i) {
                varArgs[varArgsCounter++] = args[i];
            }

            passArgs[passArgs.length - 1] = varArgs;
        } else {
            for (int i=0; i<passArgs.length; i++) {
                if (i<args.length) {
                    if (args[i]==null) passArgs[i] = null;
                    else passArgs[i] = args[i].toJavaObject(pType[i]);
                } else {
                    passArgs[i] = null;
                }
            }
        }

        JSValue returnValue;
        JSObject stack=null;
        try {
            stack = invokeObject.getThis();
            invokeObject.setThis(thiz);
            Object ret = method.invoke(invokeObject, passArgs);
            if (ret == null) {
                returnValue = null;
            } else if (ret instanceof JSValue) {
                returnValue = (JSValue) ret;
            } else {
                returnValue = new JSValue(context, ret);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            context.throwJSException(new JSException(context, e.toString()));
            returnValue = null;
        } catch (IllegalAccessException e) {
            context.throwJSException(new JSException(context, e.toString()));
            returnValue = null;
        } finally {
            invokeObject.setThis(stack);
        }
        return returnValue;
    }

    protected void constructor(final JNIJSObject thisObj, final JSValue [] args) {
        try {
            Constructor<?> defaultConstructor = subclass.getConstructor();
            final JSObject thiz = (JSObject) defaultConstructor.newInstance();
            thiz.context = context;
            thiz.valueRef = thisObj;
            thiz.addJSExports();
            function(thiz,args);
            context.persistObject(thiz);
            context.zombies.add(thiz);
        } catch (NoSuchMethodException e) {
            String error = e.toString() + "If " + subclass.getName() + " is an embedded " +
                    "class, did you specify it as 'static'?";
            context.throwJSException(new JSException(context, error));
        } catch (InvocationTargetException e) {
            String error = e.toString() + "; Did you remember to call super?";
            context.throwJSException(new JSException(context, error));
        } catch (IllegalAccessException e) {
            String error = e.toString() + "; Is your constructor public?";
            context.throwJSException(new JSException(context, error));
        } catch (InstantiationException e) {
            context.throwJSException(new JSException(context, e.toString()));
        }
    }

    @SuppressWarnings("unused") // This is called directly from native code
    @Keep
    private void constructorCallback(long thisObjectRef, long[] argumentsValueRef) {
        try {
            JSValue [] args = new JSValue[argumentsValueRef.length];
            for (int i=0; i<argumentsValueRef.length; i++) {
                JNIJSValue ref = JNIJSValue.fromRef(argumentsValueRef[i]);
                if (ref.isObject()) {
                    try {
                        args[i] = context.getObjectFromRef(ref.toObject());
                    } catch (JNIJSException e) {
                        e.printStackTrace();
                        throw new AssertionError();
                    }
                } else {
                    args[i] = new JSValue(ref,context);
                }
            }
            constructor(JNIJSObject.fromRef(thisObjectRef),args);
        } catch (JSException e) {
            JNIJSFunction.setException(valueRef().reference,e.getError().valueRef().reference);
        }
    }

    private Class<? extends JSObject> subclass = null;

    /**
     * Called only by convenience subclasses.  If you use
     * this, you must set context and valueRef yourself.  Also,
     * don't forget to call protect()!
     */
    protected JSFunction() {
    }

    protected Method method = null;
    private Class<?>[] pType;
    private JSObject invokeObject = null;
}
