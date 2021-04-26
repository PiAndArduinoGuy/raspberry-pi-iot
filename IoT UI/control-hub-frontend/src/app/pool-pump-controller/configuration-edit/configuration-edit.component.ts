import {Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {BackendHttpRequestsService} from '../backend-http-requests.service';
import {PumpConfigModel} from '../pump-config.model';

@Component({
  selector: 'app-pool-pump-cofigurator',
  templateUrl: './configuration-edit.component.html',
  styleUrls: ['./configuration-edit.component.scss']
})
export class ConfigurationEditComponent implements OnInit {
  @ViewChild('form', {static: false}) form: NgForm;

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private backendHttpRequestsService: BackendHttpRequestsService) { }

  ngOnInit(): void {
    this.backendHttpRequestsService.getPumpConfiguration().subscribe(
      (pumpConfig: PumpConfigModel) => {
        console.log(pumpConfig);
        this.form.setValue({
          turnOnTemp : pumpConfig.turnOnTemp,
          overrideStatus : pumpConfig.overrideStatus
        });
    }
    );
  }


  onSaveNewConfiguration() {
    const newPumpConfig = new PumpConfigModel();
    newPumpConfig.overrideStatus = this.form.value.overrideStatus;
    newPumpConfig.turnOnTemp = this.form.value.turnOnTemp;

    this.backendHttpRequestsService.saveNewPumpConfig(newPumpConfig);
    this.router.navigate(['../'], {relativeTo: this.activatedRoute});
  }
}
