package com.bugsnag.android.repackaged.dslplatform.json;

import java.util.*;

class ExternalConverterAnalyzer {
	private final Set<String> lookedUpClasses = new HashSet<String>();
	private final ClassLoader[] classLoaders;

	ExternalConverterAnalyzer(Collection<ClassLoader> classLoaders) {
		this.classLoaders = classLoaders.toArray(new ClassLoader[0]);
	}

	synchronized boolean tryFindConverter(Class<?> manifest, DslJson<?> dslJson) {
		final String className = manifest.getName();
		if (!lookedUpClasses.add(className)) return false;
		String[] converterClassNames = resolveExternalConverterClassNames(className);
		for (ClassLoader cl : classLoaders) {
			for (String ccn : converterClassNames) {
				try {
					Class<?> converterClass = cl.loadClass(ccn);
					if (!Configuration.class.isAssignableFrom(converterClass)) continue;
					Configuration converter = (Configuration) converterClass.newInstance();
					converter.configure(dslJson);
					return true;
				} catch (ClassNotFoundException ignored) {
				} catch (IllegalAccessException ignored) {
				} catch (InstantiationException ignored) {
				}
			}
		}
		return false;
	}

	private String[] resolveExternalConverterClassNames(final String fullClassName) {
		int dotIndex = fullClassName.lastIndexOf('.');
		if (dotIndex == -1) {
			return new String[]{String.format("_%s_DslJsonConverter", fullClassName)};
		}
		String packageName = fullClassName.substring(0, dotIndex);
		String className = fullClassName.substring(dotIndex + 1);
		return new String[]{
				String.format("%s._%s_DslJsonConverter", packageName, className),
				String.format("dsl_json.%s._%s_DslJsonConverter", packageName, className),
				String.format("dsl_json.%s.%sDslJsonConverter", packageName, className)};
	}
}