package com.github.dynamicextensionsalfresco.controlpanel.template;

import com.github.dynamicextensionsalfresco.controlpanel.BundleHelper;
import com.springsource.util.osgi.manifest.BundleManifest;
import com.springsource.util.osgi.manifest.BundleManifestFactory;
import com.springsource.util.osgi.manifest.ExportedPackage;
import com.springsource.util.osgi.manifest.ImportedPackage;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Adapts an {@link Bundle} for display in a Freemarker template.
 * 
 * @author Laurens Fridael
 * 
 */
public class TemplateBundle implements Comparable<TemplateBundle> {

	private static final int FRAMEWORK_BUNDLE_ID = 0;

	private final Bundle bundle;

	private BundleManifest manifest;

	private final List<TemplateServiceReference> services;

	@SuppressWarnings("rawtypes")
	public TemplateBundle(final Bundle bundle, final List<ServiceReference> services) {
		Assert.notNull(bundle);
		this.bundle = bundle;
		if (services != null) {
			this.services = createTemplateServices(services);
		} else {
			this.services = Collections.emptyList();
		}
	}

	public TemplateBundle(final Bundle bundle) {
		this(bundle, null);
	}

	public long getBundleId() {
		return bundle.getBundleId();
	}

	public String getSymbolicName() {
		return bundle.getSymbolicName();
	}

	public String getName() {
		return toString(bundle.getHeaders().get(Constants.BUNDLE_NAME));
	}

	public String getDescription() {
		return toString(bundle.getHeaders().get(Constants.BUNDLE_DESCRIPTION));
	}

	public boolean isDynamicExtension() {
		return BundleHelper.Companion.isDynamicExtension(bundle);
	}

	public boolean isFragmentBundle() {
		return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
	}

	public String getLocation() {
		return bundle.getLocation();
	}

	public String getLastModified() {
		final long lastModified = bundle.getLastModified();
		if (lastModified > 0) {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(new Date(lastModified));
		} else {
			return null;
		}
	}

	public String getVersion() {
		return bundle.getVersion().toString();
	}

	public String getStore() {
		if (bundle.getLocation().startsWith("file:")) {
			return "filesystem";
		} else if (bundle.getLocation().startsWith("/")) {
			return "repository";
		} else {
			return "n/a";
		}
	}

	public String getStatus() {
		switch (bundle.getState()) {
		case Bundle.UNINSTALLED:
			return "uninstalled";
		case Bundle.INSTALLED:
			return "installed";
		case Bundle.RESOLVED:
			return "resolved";
		case Bundle.STARTING:
			return "starting";
		case Bundle.STOPPING:
			return "stopping";
		case Bundle.ACTIVE:
			return "active";
		default:
			return null;
		}
	}

	public String getExportPackage() {
		return bundle.getHeaders().get(Constants.EXPORT_PACKAGE);
	}

	public String getDocumentationUrl() {
		return bundle.getHeaders().get(Constants.BUNDLE_DOCURL);
	}

	public boolean isDeleteable() {
		return getLocation().startsWith("/Company Home");
	}

	public List<TemplateImportedPackage> getImportedPackages() {
		final List<TemplateImportedPackage> packages = new ArrayList<TemplateImportedPackage>();
		for (final ImportedPackage importedPackage : getManifest().getImportPackage().getImportedPackages()) {
			final TemplateImportedPackage bundlePackage = new TemplateImportedPackage();
			bundlePackage.setName(importedPackage.getPackageName());
			final Version ceiling = importedPackage.getVersion().getCeiling();
			if (ceiling != null) {
				bundlePackage.setMaxVersion(ceiling.toString());
			}
			final Version floor = importedPackage.getVersion().getFloor();
			if (floor != null) {
				bundlePackage.setMinVersion(floor.toString());
			}
			packages.add(bundlePackage);
		}
		return packages;
	}

	public List<ExportedPackage> getExportedPackages() {
		return getManifest().getExportPackage().getExportedPackages();
	}

	public List<TemplateServiceReference> getServices() {
		return services;
	}

	/* Utility operations */

	protected BundleManifest getManifest() {
		if (manifest == null) {
			manifest = BundleManifestFactory.createBundleManifest(bundle.getHeaders());
		}
		return manifest;
	}

	@SuppressWarnings("rawtypes")
	private static List<TemplateServiceReference> createTemplateServices(final List<ServiceReference> services) {
		final List<TemplateServiceReference> templateServices = new ArrayList<TemplateServiceReference>(services.size());
		for (final ServiceReference serviceReference : services) {
			templateServices.add(new TemplateServiceReference(serviceReference));
		}
		Collections.sort(templateServices);
		return templateServices;
	}

	@Override
	public int compareTo(final TemplateBundle other) {
		if (this.getBundleId() == FRAMEWORK_BUNDLE_ID) {
			return Integer.MIN_VALUE;
		} else if (other.getBundleId() == 0) {
			return Integer.MAX_VALUE;
		}
		final int compare = this.getName().compareToIgnoreCase(other.getName());
		if (compare == 0) {
			return this.getVersion().compareTo(other.getVersion());

		}
		return compare;
	}

	/**
	 * Utility function for working around problems when compiling against JDK 7. See <a
	 * href="https://mail.osgi.org/pipermail/osgi-dev/2011-August/003223.html">this page</a> for an explanation on the
	 * JDK 7 compile issue.
	 * 
	 * @param value
	 * @return
	 */
	private static String toString(final Object value) {
		return value != null ? value.toString() : null;
	}

}
