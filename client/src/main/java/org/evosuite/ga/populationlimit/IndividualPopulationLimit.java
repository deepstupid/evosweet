/**
 * Copyright (C) 2010-2017 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.ga.populationlimit;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;

import java.util.List;


/**
 * <p>IndividualPopulationLimit class.</p>
 *
 * @author Gordon Fraser
 */
public class IndividualPopulationLimit implements PopulationLimit {

	private static final long serialVersionUID = -3985726226793280031L;

	/* (non-Javadoc)
	 * @see org.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isPopulationFull(List<? extends Chromosome> population) {
		return population.size() >= Properties.POPULATION;
	}

}
