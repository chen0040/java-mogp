package com.github.chen0040.mogp;


import com.github.chen0040.gp.treegp.TreeGP;
import com.github.chen0040.gp.treegp.program.Solution;
import com.github.chen0040.moea.components.Mediator;

import java.util.List;


/**
 * Created by xschen on 19/6/2017.
 */
public interface CostFunction {
   List<Double> evaluateCosts(Solution solution, Mediator moeaConfig, TreeGP gpConfig);
}
