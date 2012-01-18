/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.graphics.RE.software;

import static jpcsp.util.Utilities.round;
import jpcsp.graphics.GeContext;
import jpcsp.graphics.VertexState;
import jpcsp.graphics.RE.software.Rasterizer.Range;

/**
 * @author gid15
 *
 */
public class TriangleRenderer extends BaseRenderer {
	protected VertexState v1;
	protected VertexState v2;
	protected VertexState v3;
	protected boolean initialized;

	public TriangleRenderer(VertexState v1, VertexState v2, VertexState v3) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}

	protected void initialize(GeContext context) {
		if (!initialized) {
			init(context);
			setPositions(v1, v2, v3);

			initialized = true;
		}
	}

	public boolean isCulled(GeContext context, boolean invertedFrontFace) {
		// Back face culling enabled?
        if (!context.clearMode && context.cullFaceFlag.isEnabled()) {
    		initialize(context);

        	if (context.frontFaceCw) {
        		// The visible face is clockwise
        		if (!isClockwise() ^ invertedFrontFace) {
        			if (log.isTraceEnabled()) {
        				log.trace("Counterclockwise triangle not displayed");
        			}
        			return true;
        		}
        	} else {
        		// The visible face is counterclockwise
        		if (isClockwise() ^ invertedFrontFace) {
        			if (log.isTraceEnabled()) {
        				log.trace("Clockwise triangle not displayed");
        			}
        			return true;
        		}
        	}
        }

        return false;
	}

	@Override
	public boolean prepare(GeContext context, CachedTexture texture) {
        if (log.isTraceEnabled()) {
        	log.trace(String.format("TriangleRenderer"));
        }

		initialize(context);

        if (!isVisible(context)) {
        	return false;
        }

        setTextures(v1, v2, v3);

        prepareTextureReader(context, texture, v1, v2, v3);
        prepareWriters(context);
        if (imageWriter == null || depthWriter == null) {
        	return false;
        }

        prepareFilters(context);

		return true;
	}

	@Override
	public void render() {
		if (transform2D) {
			render2D();
		} else {
			render3D();
		}
        super.render();
	}

	protected void render2D() {
		float v = vStart;
        for (int y = 0; y < destinationHeight; y++) {
        	pixel.y = pyMin + y;
    		pixel.v = v;
    		float u = uStart;
        	for (int x = 0; x < destinationWidth; x++) {
        		pixel.x = pxMin + x;
        		pixel.u = u;
        		if (isInsideTriangle()) {
        			pixel.filterPassed = true;
        			pixel.sourceDepth = round(pixel.getTriangleWeightedValue(p1z, p2z, p3z));
            		pixel.destination = imageWriter.readCurrent();
            		pixel.destinationDepth = depthWriter.readCurrent();
            		for (int i = 0; i < numberFilters; i++) {
            			pixel.source = filters[i].filter(pixel);
            			if (!pixel.filterPassed) {
            				break;
            			}
            		}
            		if (pixel.filterPassed) {
	            		imageWriter.writeNext(pixel.source);
	            		depthWriter.writeNext(pixel.sourceDepth);
            		} else {
            			// Filter did not pass, do not update the pixel
            			imageWriter.skip(1);
            			depthWriter.skip(1);
            		}
        		} else {
        			// Do not display, skip the pixel
        			imageWriter.skip(1);
        			depthWriter.skip(1);
        		}
        		u += uStep;
        	}
        	v += vStep;
        }
	}

	protected void render3D() {
		RESoftware.triangleRender3DStatistics.start();
		Range range = new Range();
		Rasterizer rasterizer = new Rasterizer(round(p1x), round(p1y), round(p2x), round(p2y), round(p3x), round(p3y));
		rasterizer.setY(pyMin);
		int numberPixels = 0;
        for (int y = pyMin; y < pyMax; y++) {
        	pixel.y = y;
        	rasterizer.getNextRange(range);
        	int startX = Math.max(range.xMin, pxMin);
        	int endX = Math.min(range.xMax, pxMax);
        	if (startX >= endX) {
        		imageWriter.skip(pxMax - pxMin);
        		depthWriter.skip(pxMax - pxMin);
        	} else {
        		numberPixels += endX - startX;
        		imageWriter.skip(startX - pxMin);
        		depthWriter.skip(startX - pxMin);
	        	for (int x = startX; x < endX; x++) {
	        		pixel.x = x;
	        		computeTriangleWeights();
	        		if (isInsideTriangle()) {
	        			// Compute the mapped texture u,v coordinates
	        			// based on the Barycentric coordinates.
	        			// Apply a perspective correction by weighting the coordinates
	        			// by their "w" value.
	        			// See http://en.wikipedia.org/wiki/Texture_mapping#Perspective_correctness
	        			float u = pixel.getTriangleWeightedValue(t1u * p1wInverted, t2u * p2wInverted, t3u * p3wInverted);
	        			float v = pixel.getTriangleWeightedValue(t1v * p1wInverted, t2v * p2wInverted, t3v * p3wInverted);
	        			float weightInverted = 1.f / pixel.getTriangleWeightedValue(p1wInverted, p2wInverted, p3wInverted);
	        			pixel.u = u * weightInverted;
	        			pixel.v = v * weightInverted;
	        			pixel.filterPassed = true;
	        			pixel.sourceDepth = round(pixel.getTriangleWeightedValue(p1z, p2z, p3z));
	        			pixel.destination = imageWriter.readCurrent();
	        			pixel.destinationDepth = depthWriter.readCurrent();
	        			for (int i = 0; i < numberFilters; i++) {
	        				pixel.source = filters[i].filter(pixel);
	        				if (!pixel.filterPassed) {
	        					break;
	        				}
	        			}
	        			if (pixel.filterPassed) {
	        	    		imageWriter.writeNext(pixel.source);
	        	    		depthWriter.writeNext(pixel.sourceDepth);
	        			} else {
	        				// Filter did not pass, do not update the pixel
	        				imageWriter.skip(1);
	        				depthWriter.skip(1);
	        			}
	        		} else {
	        			// Do not display, skip the pixel
	        			imageWriter.skip(1);
	        			depthWriter.skip(1);
	        		}
	        	}
	    		imageWriter.skip(pxMax - endX);
	    		depthWriter.skip(pxMax - endX);
        	}
        }
        if (numberPixels > 10000) {
        	log.info(String.format("render3D: %d pixels, (%d,%d)-(%d,%d), duration %dms", numberPixels, pxMin, pyMin, pxMax, pyMax, RESoftware.triangleRender3DStatistics.getDurationMillis()));
        }
        RESoftware.triangleRender3DStatistics.end();
	}
}
