package wpds.impl;

import java.util.Map;

import com.google.common.collect.Maps;

import wpds.interfaces.Empty;
import wpds.interfaces.IPushdownSystem;
import wpds.interfaces.Location;
import wpds.interfaces.ReachabilityListener;
import wpds.interfaces.State;
import wpds.interfaces.WPAStateListener;
import wpds.interfaces.WPAUpdateListener;
import wpds.interfaces.WPDSUpdateListener;
import wpds.wildcard.ExclusionWildcard;
import wpds.wildcard.Wildcard;

public class PostStar<N extends Location, D extends State, W extends Weight> {
	private IPushdownSystem<N, D, W> pds;
	private WeightedPAutomaton<N, D, W> fa;
	private Map<Transition<N,D>, WeightedPAutomaton<N, D, W>> summaries = Maps.newHashMap();
	public static boolean SUMMARIES = false;

	public void poststar(IPushdownSystem<N, D, W> pds, WeightedPAutomaton<N, D, W> initialAutomaton) {
		this.pds = pds;
		this.fa = initialAutomaton;
		this.pds.registerUpdateListener(new PostStarUpdateListener(fa));
	}
	
	
	private class PostStarUpdateListener implements WPDSUpdateListener<N, D, W> {

		private WeightedPAutomaton<N, D, W> aut;

		public PostStarUpdateListener(WeightedPAutomaton<N, D, W> fa) {
			aut = fa;
		}
		
		@Override
		public void onRuleAdded(final Rule<N, D, W> rule) {
			if(rule instanceof NormalRule){
				aut.registerListener(new HandleNormalListener((NormalRule)rule));
			} else if(rule instanceof PushRule){
				aut.registerListener(new HandlePushListener((PushRule)rule));
			} else if(rule instanceof PopRule){
				aut.registerListener(new HandlePopListener(aut, rule.getS1(), rule.getL1(),rule.getS2(),rule.getWeight()));
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((aut == null) ? 0 : aut.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PostStarUpdateListener other = (PostStarUpdateListener) obj;
			if (aut == null) {
				if (other.aut != null)
					return false;
			} else if (!aut.equals(other.aut))
				return false;
			return true;
		}

	}
	
	private class UpdateTransitivePopListener extends WPAStateListener<N, D, W> {

		private D targetState;
		private Transition<N, D> transition;

		public UpdateTransitivePopListener(Transition<N, D> transition) {
			super(transition.getTarget());
			this.transition = transition;
		}

		@Override
		public void onOutTransitionAdded(Transition<N, D> t, W w) {
			W newWeight = fa.getWeightFor(transition);
			update(new Transition<N, D>(transition.getStart(), t.getLabel(), t.getTarget()),
					(W) newWeight.extendWith(w));
		}

		@Override
		public void onInTransitionAdded(Transition<N, D> t, W w) {
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((targetState == null) ? 0 : targetState.hashCode());
			result = prime * result + ((transition == null) ? 0 : transition.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			UpdateTransitivePopListener other = (UpdateTransitivePopListener) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (targetState == null) {
				if (other.targetState != null)
					return false;
			} else if (!targetState.equals(other.targetState))
				return false;
			if (transition == null) {
				if (other.transition != null)
					return false;
			} else if (!transition.equals(other.transition))
				return false;
			return true;
		}

		private PostStar getOuterType() {
			return PostStar.this;
		}
	}
	
	private class HandlePopListener extends WPAStateListener<N, D, W> {
		private N popLabel;
		private D targetState;
		private W ruleWeight;
		private WeightedPAutomaton<N, D, W> aut;
		public HandlePopListener(WeightedPAutomaton<N, D, W> aut, D state, N popLabel, D targetState, W ruleWeight) {
			super(state);
			this.aut = aut;
			this.targetState = targetState;
			this.popLabel = popLabel;
			this.ruleWeight = ruleWeight;
		}


		@Override
		public void onOutTransitionAdded(final Transition<N, D> t, W weight) {
			if(t.getLabel().equals(popLabel) && fa.isGeneratedState(t.getTarget())){
				if(popLabel instanceof Empty){
					throw new RuntimeException("IllegalState");
				}
				final W newWeight = (W) weight.extendWith(ruleWeight);
				update(new Transition<N, D>(targetState, fa.epsilon(), t.getTarget()), newWeight);
				
				fa.registerListener(new UpdateTransitivePopListener(new Transition<N, D>(targetState, fa.epsilon(), t.getTarget())));
			}
			if(t.getLabel() instanceof Empty){
				aut.registerListener(new HandlePopListener(aut, t.getTarget(), popLabel, targetState, ruleWeight));
			}
		}

		@Override
		public void onInTransitionAdded(Transition<N, D> t, W weight) {
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((popLabel == null) ? 0 : popLabel.hashCode());
			result = prime * result + ((ruleWeight == null) ? 0 : ruleWeight.hashCode());
			result = prime * result + ((targetState == null) ? 0 : targetState.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			HandlePopListener other = (HandlePopListener) obj;
			if (popLabel == null) {
				if (other.popLabel != null)
					return false;
			} else if (!popLabel.equals(other.popLabel))
				return false;
			if (ruleWeight == null) {
				if (other.ruleWeight != null)
					return false;
			} else if (!ruleWeight.equals(other.ruleWeight))
				return false;
			if (targetState == null) {
				if (other.targetState != null)
					return false;
			} else if (!targetState.equals(other.targetState))
				return false;
			return true;
		}




	}
	
	private class HandleNormalListener extends WPAStateListener<N, D, W> {
		private NormalRule<N, D, W> rule;
		public HandleNormalListener(NormalRule<N, D, W> rule) {
			super(rule.getS1());
			this.rule = rule;
		}


		@Override
		public void onOutTransitionAdded(final Transition<N, D> t, W weight) {
			if(t.getLabel().equals(rule.getL1()) || rule.getL1() instanceof Wildcard){
				W newWeight = (W) weight.extendWith(rule.getWeight());
				D p = rule.getS2();
				N l2 = rule.getL2();
				if (l2 instanceof ExclusionWildcard) {
					ExclusionWildcard<N> ex = (ExclusionWildcard<N>) l2;
					if (t.getString().equals(ex.excludes()))
						return;
				}
				if (l2 instanceof Wildcard) {
					l2 = t.getString();
					if(l2.equals(fa.epsilon()))
						return;
				}
				
				update(new Transition<N, D>(p, l2, t.getTarget()), newWeight);
			}
		}

		@Override
		public void onInTransitionAdded(Transition<N, D> t, W weight) {
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((rule == null) ? 0 : rule.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			HandleNormalListener other = (HandleNormalListener) obj;
			if (rule == null) {
				if (other.rule != null)
					return false;
			} else if (!rule.equals(other.rule))
				return false;
			return true;
		}

	}

	
	private class HandlePushListener extends WPAStateListener<N, D, W> {
		private PushRule<N, D, W> rule;
		
		public HandlePushListener(PushRule<N, D, W> rule) {
			super(rule.getS1());
			this.rule = rule;
		}


		@Override
		public void onOutTransitionAdded(final Transition<N, D> t, W weight) {
			if(t.getLabel().equals(rule.getL1()) || rule.getL1() instanceof Wildcard){
				if(rule.getCallSite() instanceof Wildcard){
					if(t.getLabel().equals(fa.epsilon()))
						return;
				}
				final D p = rule.getS2();
				final N gammaPrime = rule.getL2();
				final D irState = fa.createState(p, gammaPrime);
				if(!SUMMARIES){
					update(new Transition<N, D>(p, gammaPrime, irState),fa.getOne());
				} else{
					if(!fa.isGeneratedState(irState))
						throw new RuntimeException("State must be generated");
					final WeightedPAutomaton<N, D, W> summary = getOrCreateSummaryAutomaton(new Transition<N, D>(p, gammaPrime, irState), fa.getOne());
					summary.registerListener(new WPAUpdateListener<N, D, W>() {

						@Override
						public void onWeightAdded(Transition<N, D> t, W w) {
							if((t.getLabel().equals(fa.epsilon()) && t.getTarget().equals(irState))){
								update(t, (W) w);
							}
						}
					});
				}
				
				final N transitionLabel = (rule.getCallSite() instanceof Wildcard ? t.getLabel() : rule.getCallSite());
				update(new Transition<N, D>(irState, transitionLabel, t.getTarget()), (W)weight.extendWith(rule.getWeight()));
				fa.registerListener(new UpdateEpsilonOnPushListener(new Transition<N, D>(irState, transitionLabel, t.getTarget()), rule.getL1()));
			}
		}

		@Override
		public void onInTransitionAdded(Transition<N, D> t, W weight) {
			
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((rule == null) ? 0 : rule.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			HandlePushListener other = (HandlePushListener) obj;
			if (rule == null) {
				if (other.rule != null)
					return false;
			} else if (!rule.equals(other.rule))
				return false;
			return true;
		}

	}
	
	private class UpdateEpsilonOnPushListener extends WPAStateListener<N, D, W>{
		private Transition<N, D> transition;
		private N callSite;

		public UpdateEpsilonOnPushListener(Transition<N, D> transition, N callSite){
			super(transition.getStart());
			this.transition = transition;
			this.callSite = callSite;
		}

		@Override
		public void onOutTransitionAdded(Transition<N, D> t, W weight) {
		}

		@Override
		public void onInTransitionAdded(Transition<N, D> t, W weight) {
			if (t.getString().equals(fa.epsilon())) {
				W newWeight = fa.getWeightFor(transition);
				update(new Transition<N, D>(t.getStart(), transition.getLabel(), transition.getTarget()),
						(W) newWeight.extendWith(weight));
				fa.reconnectPush(callSite, transition.getLabel(),t.getStart(), newWeight, weight);
			}	
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((callSite == null) ? 0 : callSite.hashCode());
			result = prime * result + ((transition == null) ? 0 : transition.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			UpdateEpsilonOnPushListener other = (UpdateEpsilonOnPushListener) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (callSite == null) {
				if (other.callSite != null)
					return false;
			} else if (!callSite.equals(other.callSite))
				return false;
			if (transition == null) {
				if (other.transition != null)
					return false;
			} else if (!transition.equals(other.transition))
				return false;
			return true;
		}

		private PostStar getOuterType() {
			return PostStar.this;
		};


		
	}
	
	private boolean update(Transition<N, D> trans, W weight) {
		return fa.addWeightForTransition(trans, weight);
	}


	private WeightedPAutomaton<N, D, W> getOrCreateSummaryAutomaton(Transition<N, D> transition, W weight) {
		WeightedPAutomaton<N, D, W> aut = getSummaries().get(transition);
		if(aut == null){
			aut = fa.createNestedAutomaton();
			getSummaries().put(transition, aut);
		}
		new PostStar<N, D, W>(){
			protected Map<Transition<N,D>,WeightedPAutomaton<N,D,W>> getSummaries() {
				return PostStar.this.getSummaries();
			};
		}.poststar(pds, aut);
		aut.addWeightForTransition(transition, weight);
		return aut;
	}
	

	protected Map<Transition<N, D>, WeightedPAutomaton<N, D, W>> getSummaries(){
		return summaries;
	}
}
