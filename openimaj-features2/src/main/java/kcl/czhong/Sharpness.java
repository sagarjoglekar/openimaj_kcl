/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.image.feature.global;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.mask.AbstractMaskedObject;
import org.openimaj.image.processing.convolution.AverageNxM;
import org.openimaj.image.processing.convolution.Laplacian3x3;

/**
 * Sharpness measures the clarity and level of detail of an image. This class
 * measures the Sharpness of an image as a function of its Laplacian, normalized
 * by the local average luminance in the surroundings of each pixel.
 * 
 * @author Jonathon Hare
 * 
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Jose San Pedro", "Stefan Siersdorfer" },
		title = "Ranking and Classifying Attractiveness of Photos in Folksonomies",
		year = "2009",
		booktitle = "18th International World Wide Web Conference",
		pages = { "771", "", "771" },
		url = "http://www2009.eprints.org/78/",
		month = "April")
public class Sharpness extends AbstractMaskedObject<FImage>
		implements
			ImageAnalyser<FImage>,
			FeatureVectorProvider<DoubleFV>
{
	private final Laplacian3x3 laplacian = new Laplacian3x3();
	private final AverageNxM average = new AverageNxM(3, 3);

	protected double sharpness;

	/**
	 * Construct with no mask set
	 */
	public Sharpness() {
		super();
	}

	/**
	 * Construct with a mask.
	 * 
	 * @param mask
	 *            the mask.
	 */
	public Sharpness(FImage mask) {
		super(mask);
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { sharpness });
	}

	@Override
	public void analyseImage(FImage image) {
		final FImage limg = image.process(laplacian);
		final FImage aimg = image.process(average);

		double sum = 0;
		for (int r = 0; r < limg.height; r++) {
			for (int c = 0; c < limg.width; c++) {
				if (mask != null && mask.pixels[r][c] == 0)
					continue;

				if (aimg.pixels[r][c] != 0) {
					sum += Math.abs(limg.pixels[r][c] / aimg.pixels[r][c]);
				}
			}
		}

		sharpness = sum / (limg.height * limg.width);
	}

	/**
	 * Get the sharpness of the last image processed with
	 * {@link #analyseImage(FImage)}.
	 * 
	 * @return the sharpness value
	 */
	public double getSharpness() {
		return sharpness;
	}
}
