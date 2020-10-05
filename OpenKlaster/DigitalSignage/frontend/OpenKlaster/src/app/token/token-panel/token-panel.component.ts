import {Component, OnInit} from '@angular/core';
import {TokenPanelService} from '../token-panel.service';
import {AppComponent} from '../../app.component';

@Component({
  selector: 'app-token-panel',
  templateUrl: './token-panel.component.html',
  styleUrls: ['./token-panel.component.css']
})
export class TokenPanelComponent implements OnInit {
  tokens
  cookieService
  requestReceivedState = 'wait'
  sendRequestState = 'wait'
  submittedObjectName = 'Token'

  //TODO change `service` variable name
  //MM-ANSWER Done
  constructor(public tokenPanelService: TokenPanelService, public appComp: AppComponent) {
    this.cookieService = appComp.cookieService;
  }

  ngOnInit(): void {
    let request = this.tokenPanelService.getTokens(this.appComp.cookieService);
    request.subscribe(response => {
      this.tokens = response["userTokens"]
    })
  }

  addToken() {
    this.sendRequestState = 'sent'
    let addPromise = this.tokenPanelService.generateToken(this.cookieService).toPromise();
    addPromise
      .then(() => {
        this.sendRequestState = 'received'
        this.requestReceivedState = 'success'
        this.ngOnInit()
      })
      .catch(() => {
        this.sendRequestState = 'received'
        this.requestReceivedState = 'fail'
        this.ngOnInit()
      })
  }
}
