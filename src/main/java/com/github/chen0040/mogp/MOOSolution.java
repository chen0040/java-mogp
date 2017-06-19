package com.github.chen0040.mogp;


import com.github.chen0040.moea.components.Mediator;
import com.github.chen0040.moea.components.Solution;
import lombok.Getter;
import lombok.Setter;


/**
 * Created by xschen on 19/6/2017.
 */
public class MOOSolution extends Solution {

   @Getter
   @Setter
   private com.github.chen0040.gp.treegp.program.Solution forest = new com.github.chen0040.gp.treegp.program.Solution();


   @Override public Solution makeCopy() {
      MOOSolution clone = new MOOSolution();
      clone.copy(this);
      return clone;
   }

   @Override public void copy(Solution rhs) {
      MOOSolution that = (MOOSolution)rhs;
      super.copy(that);
      forest.copy(that.forest);
   }

   @Override
   public void evaluate(Mediator mediator) {

   }

}
