package com.github.chen0040.mogp;


import com.github.chen0040.data.exceptions.NotImplementedException;
import com.github.chen0040.moea.components.Mediator;
import com.github.chen0040.moea.components.Solution;
import lombok.Getter;
import lombok.Setter;


/**
 * Created by xschen on 19/6/2017.
 */
public class MOOGPSolution extends Solution {

   @Getter
   @Setter
   private com.github.chen0040.gp.treegp.program.Solution gp = new com.github.chen0040.gp.treegp.program.Solution();


   @Override public Solution makeCopy() {
      MOOGPSolution clone = new MOOGPSolution();
      clone.copy(this);
      return clone;
   }

   @Override public void copy(Solution rhs) {
      MOOGPSolution that = (MOOGPSolution)rhs;
      super.copy(that);
      gp.copy(that.gp);
   }

   @Override
   public void evaluate(Mediator mediator) {
      throw new NotImplementedException();
   }


   @Override public void initialize(Mediator mediator) {
      data.clear();
   }


   @Override public void mutateUniformly(Mediator mediator) {
      throw new NotImplementedException();
   }


   @Override public void onePointCrossover(Mediator mediator, Solution rhs) {
      throw new NotImplementedException();
   }


   @Override public void uniformCrossover(Mediator mediator, Solution rhs) {
      throw new NotImplementedException();
   }


   @Override public void evaluate(Mediator mediator, int objective_index) {
      throw new NotImplementedException();
   }
}
