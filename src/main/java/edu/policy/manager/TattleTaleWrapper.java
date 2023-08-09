package edu.policy.manager;

import edu.policy.model.constraint.Cell;
import edu.policy.model.data.Session;

import java.util.*;
import java.util.stream.Collectors;

public class TattleTaleWrapper {

    Session session;

    GreedyAlgorithm algo;

    Set<Cell> hideCellsWrapper = new HashSet<>();

    public TattleTaleWrapper(Session session) {
        this.session = session;
    }

    public void run() {
        if (!session.getPagination() || session.getBinning_size() <= 1) {
            // store sensitive policies
            List<Cell> senCellsSave = session.getPolicies();

            runMain(session);

            // save back policies
            session.setPolicies(senCellsSave);
        }
        else
            binningAndMerging(session);

        hideCellsWrapper = session.getHideCells();
    }

    Session runMain(Session curSession) {

        Set<Cell> hideCells = new HashSet<>();

        if (curSession.getAlgo().equals("full-den")) {
            algo = new GreedyPerfectSecrecy(curSession);
            algo.setUsingAlgorithm("Perfect Deniability");
            hideCells.addAll(((GreedyPerfectSecrecy) algo).greedyHolisticPerfectDen());
        }
        else if (curSession.getAlgo().equals("k-den")) {
            algo = new GreedyKSecrecy(curSession);
            algo.setUsingAlgorithm("K-value Deniability");
            hideCells.addAll(((GreedyKSecrecy) algo).greedyHolisticKDen());
        }
        else if (curSession.getAlgo().equals("full-modified")) {
            algo = new GreedyPerfectSecrecyModified(curSession);
            algo.setUsingAlgorithm("Full Modified");
            hideCells.addAll(((GreedyPerfectSecrecyModified) algo).greedyHolisticPerfectDen());
        }

        curSession.setHideCells(hideCells);

        List<Cell> hiddenCellsToPolicies = new ArrayList<>(hideCells);
        curSession.setPolicies(hiddenCellsToPolicies);
        return curSession;
    }

    void binningAndMerging(Session session) {
        Queue<Session> binQueue = binning(session.getBinning_size());
        Queue<Session> mergeQueue = new LinkedList<>();

        while (binQueue.size() != 1 || !mergeQueue.isEmpty()) {
            Session curBin = binQueue.poll();
            assert curBin != null;
            mergeQueue.add(runMain(curBin));

            if (mergeQueue.size() >= session.getMerging_size() || binQueue.isEmpty()) {
                Session mergedBin = merging(mergeQueue);
                binQueue.add(runMain(mergedBin));
                mergeQueue.clear();
            }
        }

        session.setHideCells(binQueue.poll().getHideCells());
    }

    Queue<Session> binning(int bs) {

        Queue<Session> binQueue = new LinkedList<>();
        int DBSize = session.getLimit();
        for (int i=0; i<bs; i++)
            binQueue.add(new Session(session, calcStartTupleIndex(i, bs, DBSize), calcEndTupleIndex(i, bs, DBSize),
                    filterPolicies(i, bs, DBSize, session.getPolicies())));
        return binQueue;
    }

    Session merging(Queue<Session> mergeQueue) {
        int startTupleID = 0;
        int endTupleID = 0;
        Set<Cell> mergedPolicies = new HashSet<>();

        for (Session curSession: mergeQueue) {
            if (curSession.getTupleStart() < startTupleID)
                startTupleID = curSession.getTupleStart();
            if (curSession.getTupleEnd() > endTupleID)
                endTupleID = curSession.getTupleEnd();
            mergedPolicies.addAll(curSession.getHideCells());
        }

        List<Cell> mergedPoliciesList = new ArrayList<>(mergedPolicies);

        return new Session(session, startTupleID, endTupleID, mergedPoliciesList);
    }

    int calcStartTupleIndex(int i, int bs, int DBSize) {
        return (DBSize / bs) * i;
    }

    int calcEndTupleIndex(int i, int bs, int DBSize) {
        return (DBSize / bs) * (i + 1);
    }

    List<Cell> filterPolicies(int i, int bs, int DBSize, List<Cell> policies) {
        return policies.stream().filter(cell -> (cell.getTupleID() >= (DBSize / bs) * i) &&
                (cell.getTupleID() < (DBSize / bs) * (i+1)) ).collect(Collectors.toList());
    }

    public int getTotalCuesetSize() {
        return algo.getTotalCuesetSize();
    }

    public List<Integer> getHiddenCellsFanOut() {
        return algo.getHiddenCellsFanOut();
    }

    public List<Integer> getCueSetsFanOut() {
        return algo.getCueSetsFanOut();
    }

    public Set<Cell> getHideCells() {
        return hideCellsWrapper;
    }

}
