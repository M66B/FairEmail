package biweekly.property;

import java.io.File;
import java.io.IOException;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.parameter.Display;

/*
 Copyright (c) 2013-2023, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * <p>
 * Defines an image that is associated with the component that the property
 * belongs to. Multiple instances with different DISPLAY parameters can be added
 * to the component to define different images for the client to display in
 * different circumstances.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * //from a byte array
 * byte[] data = ...
 * Image image = new Image("image/png", data);
 * image.getDisplays().add(Display.BADGE);
 * event.addImage(image);
 * 
 * //referencing a URL
 * image = new Image("image/png", "http://example.com/image.png");
 * image.getDisplays().add(Display.THUMBNAIL);
 * image.setOnClickUri("http://example.com");
 * event.addImage(image);
 * </pre>
 * @author Michael Angstadt
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-10">draft-ietf-calext-extensions-01
 * p.10</a>
 */
public class Image extends BinaryProperty {
	/**
	 * Creates a new attachment.
	 * @param formatType the content-type of the data (e.g. "image/png")
	 * @param file the file to attach
	 * @throws IOException if there's a problem reading from the file
	 */
	public Image(String formatType, File file) throws IOException {
		super(file);
		setFormatType(formatType);
	}

	/**
	 * Creates a new attachment.
	 * @param formatType the content-type of the data (e.g. "image/png")
	 * @param data the binary data
	 */
	public Image(String formatType, byte[] data) {
		super(data);
		setFormatType(formatType);
	}

	/**
	 * Creates a new attachment.
	 * @param formatType the content-type of the data (e.g. "image/png")
	 * @param uri a URL pointing to the resource (e.g.
	 * "http://example.com/image.png")
	 */
	public Image(String formatType, String uri) {
		super(uri);
		setFormatType(formatType);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Image(Image original) {
		super(original);
	}

	/**
	 * Gets the URI to go to when the user clicks on the image.
	 * @return the URI or null if not set
	 */
	public String getOnClickUri() {
		return parameters.getAltRepresentation();
	}

	/**
	 * Sets the URI to go to when the user clicks on the image.
	 * @param uri the URI or null to remove
	 */
	public void setOnClickUri(String uri) {
		parameters.setAltRepresentation(uri);
	}

	/**
	 * Gets the list that holds the ways in which the client should display this
	 * image (for example, as a thumbnail-sized image).
	 * @return the display methods (this list is mutable)
	 */
	public List<Display> getDisplays() {
		return parameters.getDisplays();
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		super.validate(components, version, warnings);
		if (data != null && getFormatType() == null) {
			warnings.add(new ValidationWarning(56));
		}
	}

	@Override
	public Image copy() {
		return new Image(this);
	}
}
