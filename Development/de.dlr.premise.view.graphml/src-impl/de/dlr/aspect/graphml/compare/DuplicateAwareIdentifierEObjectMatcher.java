/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.aspect.graphml.compare;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.match.eobject.CachingDistance;
import org.eclipse.emf.compare.match.eobject.EditionDistance;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.IdentifierEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.WeightProvider;
import org.eclipse.emf.compare.match.eobject.WeightProviderDescriptorRegistryImpl;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DuplicateAwareIdentifierEObjectMatcher extends IdentifierEObjectMatcher {

    private Function<EObject, String> idComputation = new DefaultIDFunction();

    public DuplicateAwareIdentifierEObjectMatcher() {
        super();
    }

    public DuplicateAwareIdentifierEObjectMatcher(Function<EObject, String> idComputation) {
        super(idComputation);
        this.idComputation = idComputation;
    }

    public DuplicateAwareIdentifierEObjectMatcher(IEObjectMatcher delegateWhenNoID, Function<EObject, String> idComputation) {
        super(delegateWhenNoID, idComputation);
        this.idComputation = idComputation;
    }

    public DuplicateAwareIdentifierEObjectMatcher(IEObjectMatcher delegateWhenNoID) {
        super(delegateWhenNoID);
    }

    public static IEObjectMatcher create() {
        return create(WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
    }

    public static IEObjectMatcher create(WeightProvider.Descriptor.Registry weightProviderRegistry) {
        final EditionDistance editionDistance = new EditionDistance(weightProviderRegistry);
        final CachingDistance cachedDistance = new CachingDistance(editionDistance);

        final IEObjectMatcher contentMatcher = new ProximityEObjectMatcher(cachedDistance);
        final IEObjectMatcher matcher = new DuplicateAwareIdentifierEObjectMatcher(contentMatcher);

        return matcher;
    }

    /**
     * Matches the EObject per ID.
     * 
     * Extends the superclass implementation to better handle duplicated ids in one of the input models. Any elements with duplicated ids
     * are completely skipped over and not matched at all. This means duplicated id is treated in the same way as no id.
     * 
     * In consequence, these elements are later matched by the delegate, which may apply further heuristics.
     * 
     * @param leftEObjects the objects to match (left side).
     * @param rightEObjects the objects to match (right side).
     * @param originEObjects the objects to match (origin side).
     * @param leftEObjectsNoID remaining left objects after matching
     * @param rightEObjectsNoID remaining right objects after matching
     * @param originEObjectsNoID remaining origin objects after matching
     * @return the match built in the process.
     */
    protected Set<Match> matchPerId(Iterator<? extends EObject> leftEObjects, Iterator<? extends EObject> rightEObjects,
            Iterator<? extends EObject> originEObjects, final List<EObject> leftEObjectsNoID, final List<EObject> rightEObjectsNoID,
            final List<EObject> originEObjectsNoID) {
        final Set<Match> matches = Sets.newLinkedHashSet();
        // This lookup map will be used by iterations on right and origin to find the match in which they
        // should add themselves
        final Map<String, Match> idToMatch = Maps.newHashMap();

        // We will try and mimic the structure of the input model.
        // These map do not need to be ordered, we only need fast lookup.
        final Map<EObject, Match> leftEObjectsToMatch = Maps.newHashMap();
        final Map<EObject, Match> rightEObjectsToMatch = Maps.newHashMap();
        final Map<EObject, Match> originEObjectsToMatch = Maps.newHashMap();

        // Record duplicated ids
        final Set<String> duplicateIds = Sets.newHashSet();

        // Keep track of ids for each model. Ids are added to the respective set when first encountered. If later an id is encountered
        // which is already in the set of ids for the model, we know that we have a duplicate.
        final Set<String> leftIds = Sets.newHashSet();
        final Set<String> rightIds = Sets.newHashSet();
        final Set<String> originIds = Sets.newHashSet();

        // We'll only iterate once on each of the three sides, building the matches as we go
        while (leftEObjects.hasNext()) {
            final EObject left = leftEObjects.next();

            final String identifier = idComputation.apply(left);
            
            if (leftIds.contains(identifier)) {
                duplicateIds.add(identifier);
            }
            
            if (duplicateIds.contains(identifier)) {
                removeMatchForId(identifier, idToMatch, matches, leftEObjectsToMatch, rightEObjectsToMatch, originEObjectsToMatch,
                                 leftEObjectsNoID, rightEObjectsNoID, originEObjectsNoID);
                leftEObjectsNoID.add(left);
            } else if (identifier != null) {
                leftIds.add(identifier);
                
                final Match match = CompareFactory.eINSTANCE.createMatch();
                match.setLeft(left);

                // Can we find a parent? Assume we're iterating in containment order
                final EObject parentEObject = left.eContainer();
                final Match parent = leftEObjectsToMatch.get(parentEObject);
                if (parent != null) {
                    parent.getSubmatches().add(match);
                } else {
                    matches.add(match);
                }

                idToMatch.put(identifier, match);
                leftEObjectsToMatch.put(left, match);
            } else {
                leftEObjectsNoID.add(left);
            }
        }

        while (rightEObjects.hasNext()) {
            final EObject right = rightEObjects.next();

            final String identifier = idComputation.apply(right);

            if (rightIds.contains(identifier)) {
                duplicateIds.add(identifier);
            }

            if (duplicateIds.contains(identifier)) {
                removeMatchForId(identifier, idToMatch, matches, leftEObjectsToMatch, rightEObjectsToMatch, originEObjectsToMatch,
                                 leftEObjectsNoID, rightEObjectsNoID, originEObjectsNoID);
                rightEObjectsNoID.add(right);
            } else if (identifier != null) {
                rightIds.add(identifier);

                Match match = idToMatch.get(identifier);
                if (match != null) {
                    match.setRight(right);

                    rightEObjectsToMatch.put(right, match);
                } else {
                    // Otherwise, create and place it.
                    match = CompareFactory.eINSTANCE.createMatch();
                    match.setRight(right);

                    // Can we find a parent?
                    final EObject parentEObject = right.eContainer();
                    final Match parent = rightEObjectsToMatch.get(parentEObject);
                    if (parent != null) {
                        parent.getSubmatches().add(match);
                    } else {
                        matches.add(match);
                    }

                    rightEObjectsToMatch.put(right, match);
                    idToMatch.put(identifier, match);
                }
            } else {
                rightEObjectsNoID.add(right);
            }
        }

        while (originEObjects.hasNext()) {
            final EObject origin = originEObjects.next();

            final String identifier = idComputation.apply(origin);
            
            if (originIds.contains(identifier)) {
                duplicateIds.add(identifier);
            }

            if (duplicateIds.contains(identifier)) {
                removeMatchForId(identifier, idToMatch, matches, leftEObjectsToMatch, rightEObjectsToMatch, originEObjectsToMatch,
                                 leftEObjectsNoID, rightEObjectsNoID, originEObjectsNoID);
                originEObjectsNoID.add(origin);
            } else if (identifier != null) {
                originIds.add(identifier);
                
                Match match = idToMatch.get(identifier);
                if (match != null) {
                    match.setOrigin(origin);

                    originEObjectsToMatch.put(origin, match);
                } else {
                    // Otherwise, create and place it.
                    match = CompareFactory.eINSTANCE.createMatch();
                    match.setOrigin(origin);

                    // Can we find a parent?
                    final EObject parentEObject = origin.eContainer();
                    final Match parent = originEObjectsToMatch.get(parentEObject);
                    if (parent != null) {
                        parent.getSubmatches().add(match);
                    } else {
                        matches.add(match);
                    }

                    idToMatch.put(identifier, match);
                    originEObjectsToMatch.put(origin, match);
                }
            } else {
                originEObjectsNoID.add(origin);
            }
        }

        // Remove matches that are unmatches, because we want to delegate them to the heuristic matcher
        for (Iterator<Match> it = idToMatch.values().iterator(); it.hasNext();) {
            Match m = it.next();

            if (m.getLeft() == null || m.getRight() == null) {
                it.remove();
                removeMatch(m, matches, leftEObjectsToMatch, rightEObjectsToMatch, originEObjectsToMatch, leftEObjectsNoID,
                            rightEObjectsNoID, originEObjectsNoID);
            }
        }

        return matches;
    }

    protected void removeMatchForId(final String identifier, final Map<String, Match> idToMatch, final Set<Match> matches,
            final Map<EObject, Match> leftEObjectsToMatch, final Map<EObject, Match> rightEObjectsToMatch,
            final Map<EObject, Match> originEObjectsToMatch, final List<EObject> leftEObjectsNoID, final List<EObject> rightEObjectsNoID,
            final List<EObject> originEObjectsNoID) {
        // Retrieve the match to be removed
        Match offending = idToMatch.get(identifier);
        idToMatch.remove(identifier);

        removeMatch(offending, matches, leftEObjectsToMatch, rightEObjectsToMatch, originEObjectsToMatch, leftEObjectsNoID,
                    rightEObjectsNoID, originEObjectsNoID);
    }

    protected void removeMatch(Match offending, final Set<Match> matches,
            final Map<EObject, Match> leftEObjectsToMatch, final Map<EObject, Match> rightEObjectsToMatch,
            final Map<EObject, Match> originEObjectsToMatch, final List<EObject> leftEObjectsNoID, final List<EObject> rightEObjectsNoID,
            final List<EObject> originEObjectsNoID) {
        if (offending != null) {
            // Remove match from tree of matches and matches list
            Match parent = (Match) offending.eContainer();
            if (parent != null) {
                parent.getSubmatches().remove(offending);
            }
            matches.remove(offending);

            // We do want to retain the submatches however, so we convert them to global matches
            matches.addAll(offending.getSubmatches());

            // Move the previously matched elements to the unmatched list
            if (offending.getLeft() != null) {
                leftEObjectsToMatch.remove(offending.getLeft());
                leftEObjectsNoID.add(offending.getLeft());
            }
            if (offending.getRight() != null) {
                rightEObjectsToMatch.remove(offending.getRight());
                rightEObjectsNoID.add(offending.getRight());
            }
            if (offending.getOrigin() != null) {
                originEObjectsToMatch.remove(offending.getOrigin());
                originEObjectsNoID.add(offending);
            }
        }
    }

}
