package doser.server.actions.disambiguation;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import doser.entitydisambiguation.backend.DisambiguationMainService;
import doser.entitydisambiguation.backend.AbstractDisambiguationTask;
import doser.entitydisambiguation.backend.DisambiguationTaskCollective;
import doser.entitydisambiguation.backend.DisambiguationTaskSingle;
import doser.entitydisambiguation.dpo.DisambiguationRequest;
import doser.entitydisambiguation.dpo.DisambiguationResponse;
import doser.entitydisambiguation.dpo.EntityDisambiguationDPO;
import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.properties.Properties;

@Controller
@RequestMapping("/disambiguation")
public class DisambiguationService {

	public DisambiguationService() {
		super();
	}

	/**
	 * Testing
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/disambiguateWithoutCategories-single", method = RequestMethod.POST, headers = "Accept=application/json")
	public @ResponseBody DisambiguationResponse annotateSingle(@RequestBody final DisambiguationRequest request) {
		DisambiguationResponse annotationResponse = disambiguateSingle(request);
		return annotationResponse;
	}

	@RequestMapping(value = "/disambiguationWithoutCategories-collective", method = RequestMethod.POST, headers = "Accept=application/json")
	public @ResponseBody DisambiguationResponse annotateCollectiveWithoutCategories(
			@RequestBody final DisambiguationRequest request) {
		final DisambiguationResponse response = new DisambiguationResponse();
		final DisambiguationMainService mainService = DisambiguationMainService.getInstance();
		final List<EntityDisambiguationDPO> listToDis = request.getSurfaceFormsToDisambiguate();

		if (mainService != null) {
			final List<AbstractDisambiguationTask> tasks = new LinkedList<AbstractDisambiguationTask>();
			DisambiguationTaskCollective collectiveTask = new DisambiguationTaskCollective(listToDis,
					request.getMainTopic());
			collectiveTask.setKbIdentifier("default", "EntityCentric");
			collectiveTask.setReturnNr(1000);
			tasks.add(collectiveTask);
			mainService.disambiguate(tasks);

			List<Response> responses = collectiveTask.getResponse();
			response.setTasks(responses);
			response.setDocumentUri(request.getDocumentUri());
		}
		return response;
	}

	@RequestMapping(value = "/disambiguationWithoutCategoriesBiomed-collective", method = RequestMethod.POST, headers = "Accept=application/json")
	public @ResponseBody DisambiguationResponse annotateCollectiveWithoutCategoriesBiomed(
			@RequestBody final DisambiguationRequest request) {
		final DisambiguationResponse response = new DisambiguationResponse();
		final DisambiguationMainService mainService = DisambiguationMainService.getInstance();
		final List<EntityDisambiguationDPO> listToDis = request.getSurfaceFormsToDisambiguate();

		if (mainService != null) {
			final List<AbstractDisambiguationTask> tasks = new LinkedList<AbstractDisambiguationTask>();
			DisambiguationTaskCollective collectiveTask = new DisambiguationTaskCollective(listToDis,
					request.getMainTopic());
			collectiveTask.setKbIdentifier("biomed", "EntityCentric");
			collectiveTask.setReturnNr(1000);
			tasks.add(collectiveTask);
			mainService.disambiguate(tasks);

			List<Response> responses = collectiveTask.getResponse();
			response.setTasks(responses);
			response.setDocumentUri(request.getDocumentUri());
		}
		return response;
	}

	private DisambiguationResponse disambiguateSingle(DisambiguationRequest request) {
		final DisambiguationResponse response = new DisambiguationResponse();
		final List<EntityDisambiguationDPO> listToDis = request.getSurfaceFormsToDisambiguate();
		List<Response> responseList = new LinkedList<Response>();
		response.setDocumentUri(request.getDocumentUri());
		final List<AbstractDisambiguationTask> tasks = new LinkedList<AbstractDisambiguationTask>();
		final DisambiguationMainService mainService = DisambiguationMainService.getInstance();
		if (mainService != null) {
			int docsToReturn = 0;
			if (request.getDocsToReturn() == null) {
				docsToReturn = Properties.getInstance().getDisambiguationResultSize();
			} else {
				docsToReturn = request.getDocsToReturn();
			}
			for (int i = 0; i < listToDis.size(); i++) {
				EntityDisambiguationDPO dpo = listToDis.get(i);
				DisambiguationTaskSingle task = new DisambiguationTaskSingle(dpo);
				task.setReturnNr(docsToReturn);
				task.setKbIdentifier(listToDis.get(i).getKbversion(), listToDis.get(i).getSetting());
				// Bugfix! Selected text may not be null. Should be ""
				// String instead;
				if (dpo.getSelectedText() != null) {
					tasks.add(task);
				}
			}
			mainService.disambiguate(tasks);
		}

		for (AbstractDisambiguationTask task : tasks) {
			responseList.add(task.getResponse().get(0));
		}
		response.setTasks(responseList);
		return response;
	}
}