/*
 * Copyright (c) 2018 Eric Lange
 *
 * Distributed under the MIT License.  See LICENSE.md at
 * https://github.com/LiquidPlayer/LiquidCore for terms and conditions.
 */
#include "V82JSC.h"

using namespace v8;

/**
 * Returns the module's current status.
 */
Module::Status Module::GetStatus() const
{
    assert(0);
}

/**
 * For a module in kErrored status, this returns the corresponding exception.
 */
Local<v8::Value> Module::GetException() const
{
    assert(0);
}

/**
 * Returns the number of modules requested by this module.
 */
int Module::GetModuleRequestsLength() const
{
    assert(0);
}

/**
 * Returns the ith module specifier in this module.
 * i must be < GetModuleRequestsLength() and >= 0.
 */
Local<String> Module::GetModuleRequest(int i) const
{
    assert(0);
}

/**
 * Returns the source location (line number and column number) of the ith
 * module specifier's first occurrence in this module.
 */
Location Module::GetModuleRequestLocation(int i) const
{
    assert(0);
}

/**
 * Returns the identity hash for this object.
 */
int Module::GetIdentityHash() const
{
    assert(0);
}

/**
 * ModuleDeclarationInstantiation
 *
 * Returns an empty Maybe<bool> if an exception occurred during
 * instantiation. (In the case where the callback throws an exception, that
 * exception is propagated.)
 */
Maybe<bool> Module::InstantiateModule(Local<Context> context, ResolveCallback callback)
{
    assert(0);
}

/**
 * ModuleEvaluation
 *
 * Returns the completion value.
 * TODO(neis): Be more precise or say nothing.
 */
MaybeLocal<v8::Value> Module::Evaluate(Local<Context> context)
{
    assert(0);
}

/**
 * Returns the namespace object of this module. The module must have
 * been successfully instantiated before and must not be errored.
 */
Local<v8::Value> Module::GetModuleNamespace()
{
    assert(0);
}

